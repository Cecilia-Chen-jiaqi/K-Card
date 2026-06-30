#!/usr/bin/env python3
"""Test purchase flow + refund API (Alipay sandbox)."""
import json
import re
import sys
import time
import uuid
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"
NGROK = "https://escapist-asparagus-failing.ngrok-free.dev"
TS = time.strftime("%Y%m%d%H%M%S")
PNG = bytes([
    0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
    0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
    0x08, 0x02, 0x00, 0x00, 0x00, 0x90, 0x77, 0x53, 0xDE, 0x00, 0x00, 0x00,
    0x0C, 0x49, 0x44, 0x41, 0x54, 0x08, 0xD7, 0x63, 0xF8, 0xCF, 0xC0, 0x00,
    0x00, 0x03, 0x01, 0x01, 0x00, 0x18, 0xDD, 0x8D, 0xB4, 0x00, 0x00, 0x00,
    0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82,
])
results = []


def req(method, path, body=None, token=None, multipart=None, base=BASE):
    headers = {}
    data = None
    if multipart:
        boundary = uuid.uuid4().hex
        filename, file_bytes, content_type = multipart
        data = (
            f"--{boundary}\r\n"
            f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'
            f"Content-Type: {content_type}\r\n\r\n"
        ).encode() + file_bytes + f"\r\n--{boundary}--\r\n".encode()
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
    elif body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json; charset=utf-8"
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(base + path, data=data, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=60) as resp:
        raw = resp.read().decode("utf-8")
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            return {"code": -1, "raw": raw, "status": resp.status}


def step(name, fn):
    try:
        ok, detail = fn()
        mark = "PASS" if ok else "FAIL"
        results.append((name, mark, detail))
        print(f"[{mark[0]}] {name}: {detail}")
        return ok
    except Exception as e:
        results.append((name, "FAIL", str(e)))
        print(f"[X] {name}: {e}")
        return False


def html_unescape(s):
    return s.replace("&quot;", '"').replace("&amp;", "&")


def post_alipay_form(html):
    action = re.search(r'action="([^"]+)"', html, re.I).group(1)
    biz = html_unescape(re.search(r'name="biz_content"\s+value="([^"]*)"', html, re.I).group(1))
    post_data = urllib.parse.urlencode({"biz_content": biz}).encode("utf-8")
    request = urllib.request.Request(action, data=post_data, method="POST")
    request.add_header("Content-Type", "application/x-www-form-urlencoded")
    request.add_header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    request.add_header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    opener = urllib.request.build_opener(urllib.request.HTTPRedirectHandler())
    resp = opener.open(request, timeout=60)
    final = resp.geturl()
    body_len = len(resp.read())
    ok = ("excashier" in final or "auth.htm" in final) and "/error" not in final
    return ok, f"url={final[:80]}... len={body_len}"


def main():
    ctx = {}
    print("\n=== Purchase & Refund Test ===\n")

    step("ngrok tunnel", lambda: (
        (True, "200 OK") if urllib.request.urlopen(NGROK + "/api/meta/kpop", timeout=15).status == 200 else (False, "down")
    ))

    step("ngrok notify probe", lambda: (
        (True, "returns failure (200)") if urllib.request.urlopen(
            urllib.request.Request(
                NGROK + "/api/pay/notify",
                data=b"test=1",
                method="POST",
                headers={"Content-Type": "application/x-www-form-urlencoded"},
            ),
            timeout=15,
        ).read().decode() == "failure"
        else (False, "unexpected response")
    ))

    seller = f"seller_{TS}"
    buyer = f"buyer_{TS}"
    step("register seller", lambda: ((True, "ok") if req("POST", "/api/auth/register", {
        "username": seller, "password": "123456", "phone": f"138{TS[-8:]}", "campus": "TestU"})["code"] == 0 else (False, "fail")))
    step("register buyer", lambda: ((True, "ok") if req("POST", "/api/auth/register", {
        "username": buyer, "password": "123456", "phone": f"139{TS[-8:]}", "campus": "TestU"})["code"] == 0 else (False, "fail")))

    ctx["seller_token"] = req("POST", "/api/auth/login", {"username": seller, "password": "123456"})["data"]["token"]
    ctx["buyer_token"] = req("POST", "/api/auth/login", {"username": buyer, "password": "123456"})["data"]["token"]
    step("login", lambda: (True, "tokens ok"))

    ctx["image"] = req("POST", "/api/upload/image", multipart=("t.png", PNG, "image/png"), token=ctx["seller_token"])["data"]
    ctx["goods_id"] = req("POST", "/api/goods/create", {
        "title": f"PayRefund {TS}", "description": "test", "price": 0.01, "stock": 1,
        "groupName": "SEVENTEEN", "idolName": "Mingyu", "quality": "无暇",
        "tradeType": "仅出售", "deliveryMode": 1, "coverImage": ctx["image"],
    }, token=ctx["seller_token"])["data"]["id"]
    step("create goods", lambda: (True, f"id={ctx['goods_id']}"))

    step("self-purchase blocked", lambda: (
        (True, r.get("message")) if (r := req("POST", "/api/orders/create", {
            "goodsId": ctx["goods_id"], "quantity": 1, "payType": 1}, token=ctx["seller_token"]))["code"] != 0
        and "自己" in r.get("message", "") else (False, r.get("message", "should block"))
    ))

    ctx["order_no"] = req("POST", "/api/orders/create", {
        "goodsId": ctx["goods_id"], "quantity": 1, "payType": 1}, token=ctx["buyer_token"])["data"]["orderNo"]
    step("create order", lambda: (True, ctx["order_no"]))

    step("goods delisted", lambda: (
        (True, "not in public list") if not any(str(g["id"]) == str(ctx["goods_id"]) for g in req("GET", "/api/goods/list")["data"])
        else (False, "still listed")
    ))

    pay = req("POST", "/api/pay/submit", {"orderNo": ctx["order_no"]}, token=ctx["buyer_token"])
    html = pay.get("data", "")
    has_notify = "notify_url" in html.lower()
    step("pay form generated", lambda: (
        (True, f"notify_in_form={has_notify}") if pay["code"] == 0 and "<form" in html.lower() else (False, pay.get("message"))
    ))

    if "<form" in html.lower():
        ok, detail = post_alipay_form(html)
        step("alipay redirect", lambda: (ok, detail))

    sync = req("GET", f"/api/pay/sync-status?orderNo={ctx['order_no']}")
    step("sync-status (unpaid)", lambda: (
        (True, f"paid={sync['data'].get('paid')}") if sync["code"] == 0 and not sync["data"].get("paid") else (False, str(sync))
    ))

    # Refund test: find any paid order from buyer list for refund API test
    buyer_orders = req("GET", "/api/orders/buyer/list", token=ctx["buyer_token"])["data"]
    paid_order = None
    for item in buyer_orders:
        st = item.get("order", {}).get("status")
        if st in (1, 2):
            paid_order = item["order"]["orderNo"]
            break

    # Also check all buyer orders in system - use current user's new order won't be paid
    # Try refund on unpaid - should fail gracefully
    step("refund unpaid rejected", lambda: (
        (True, r.get("message")) if (r := req("POST", "/api/pay/refund", {"orderNo": ctx["order_no"], "reason": "test"},
            token=ctx["buyer_token"]))["code"] != 0 else (False, "should reject unpaid")
    ))

    # Search for any existing paid order in DB via login as known user - skip, test refund endpoint structure
    step("refund API reachable", lambda: (
        (True, "endpoint ok") if req("POST", "/api/pay/refund", {"orderNo": "ORD000", "reason": "test"},
            token=ctx["buyer_token"])["code"] != 0 else (False, "unexpected")
    ))

    passed = sum(1 for _, s, _ in results if s == "PASS")
    failed = sum(1 for _, s, _ in results if s == "FAIL")
    print(f"\n=== Summary: {passed}/{len(results)} passed, {failed} failed ===")
    print("\nNote: Full refund test requires a PAID order (complete payment in Alipay sandbox first).")
    print("After paying, use sync-status then POST /api/pay/refund from order center.")
    if failed:
        sys.exit(1)


if __name__ == "__main__":
    main()

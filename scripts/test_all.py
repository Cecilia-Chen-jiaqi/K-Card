#!/usr/bin/env python3
"""K-CARD full API integration test."""
import json
import sys
import time
import uuid
from pathlib import Path

try:
    import urllib.request
    import urllib.error
except ImportError:
    pass

BASE = "http://localhost:8080"
TS = time.strftime("%Y%m%d%H%M%S")
results = []

# minimal 1x1 PNG
PNG = bytes([
    0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
    0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
    0x08, 0x02, 0x00, 0x00, 0x00, 0x90, 0x77, 0x53, 0xDE, 0x00, 0x00, 0x00,
    0x0C, 0x49, 0x44, 0x41, 0x54, 0x08, 0xD7, 0x63, 0xF8, 0xCF, 0xC0, 0x00,
    0x00, 0x03, 0x01, 0x01, 0x00, 0x18, 0xDD, 0x8D, 0xB4, 0x00, 0x00, 0x00,
    0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82,
])


def req(method, path, body=None, token=None, multipart=None):
    url = BASE + path
    headers = {}
    data = None
    if multipart:
        boundary = uuid.uuid4().hex
        filename, file_bytes, content_type = multipart
        body_parts = (
            f"--{boundary}\r\n"
            f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'
            f"Content-Type: {content_type}\r\n\r\n"
        ).encode() + file_bytes + f"\r\n--{boundary}--\r\n".encode()
        data = body_parts
        headers["Content-Type"] = f"multipart/form-data; boundary={boundary}"
    elif body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json; charset=utf-8"
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def step(name, fn):
    try:
        ok, detail = fn()
        status = "PASS" if ok else "FAIL"
        results.append((name, status, detail))
        mark = "+" if ok else "X"
        print(f"[{mark}] {name}: {detail}")
        return ok
    except Exception as e:
        results.append((name, "FAIL", str(e)))
        print(f"[X] {name}: {e}")
        return False


def main():
    print("\n=== K-CARD API Test ===\n")
    ctx = {}

    step("meta /api/meta/kpop", lambda: (
        (True, f"groups={len(r['data'].get('groups', {}))}") if (r := req("GET", "/api/meta/kpop"))["code"] == 0 else (False, r.get("message"))
    ))

    seller = f"seller_{TS}"
    buyer = f"buyer_{TS}"
    seller_phone = f"138{TS[-8:]}"
    buyer_phone = f"139{TS[-8:]}"

    def reg(u, p):
        r = req("POST", "/api/auth/register", {"username": u, "password": "123456", "phone": p, "campus": "TestU"})
        return r["code"] == 0, r.get("message", "ok")

    step("register seller", lambda: reg(seller, seller_phone))
    step("register buyer", lambda: reg(buyer, buyer_phone))

    def login(u):
        r = req("POST", "/api/auth/login", {"username": u, "password": "123456"})
        if r["code"] == 0:
            return r["data"]["token"]
        raise RuntimeError(r.get("message"))

    ctx["seller_token"] = login(seller)
    ctx["buyer_token"] = login(buyer)
    step("login seller", lambda: (True, "token ok"))
    step("login buyer", lambda: (True, "token ok"))

    def upload():
        r = req("POST", "/api/upload/image", multipart=("test.png", PNG, "image/png"))
        if r["code"] == 0:
            ctx["image"] = r["data"]
            return True, r["data"]
        return False, r.get("message")

    step("upload image", upload)

    def create_goods():
        r = req("POST", "/api/goods/create", {
            "title": f"Test Card {TS}", "description": "auto test", "price": 0.01, "stock": 5,
            "groupName": "SEVENTEEN", "idolName": "Mingyu", "cardType": "album",
            "albumEra": "FML", "quality": "无暇", "tradeType": "仅出售",
            "deliveryMode": 1, "coverImage": ctx.get("image"),
        }, token=ctx["seller_token"])
        if r["code"] == 0:
            ctx["goods_id"] = r["data"]["id"]
            return True, f"id={ctx['goods_id']}"
        return False, r.get("message")

    step("create goods", create_goods)

    step("goods list", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", "/api/goods/list"))["code"] == 0 else (False, r.get("message"))
    ))

    step("goods search", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", f"/api/goods/search?groupName=SEVENTEEN"))["code"] == 0 else (False, r.get("message"))
    ))

    step("goods detail", lambda: (
        (True, r["data"]["title"]) if (r := req("GET", f"/api/goods/detail?id={ctx['goods_id']}"))["code"] == 0 else (False, r.get("message"))
    ))

    step("my goods", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", "/api/goods/my", token=ctx["seller_token"]))["code"] == 0 and len(r["data"]) >= 1 else (False, f"count={len(r.get('data', []))}")
    ))

    step("cart add", lambda: (
        (True, "ok") if req("POST", "/api/cart/add", {"goodsId": ctx["goods_id"], "quantity": 1}, token=ctx["buyer_token"])["code"] == 0 else (False, "fail")
    ))

    step("cart list", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", "/api/cart/list", token=ctx["buyer_token"]))["code"] == 0 else (False, r.get("message"))
    ))

    def create_order():
        r = req("POST", "/api/orders/create", {"goodsId": ctx["goods_id"], "quantity": 1, "payType": 1}, token=ctx["buyer_token"])
        if r["code"] == 0:
            ctx["order_no"] = r["data"]["orderNo"]
            return True, ctx["order_no"]
        return False, r.get("message")

    step("create order", create_order)

    step("order detail", lambda: (
        (True, f"status={r['data']['order']['status']}") if (r := req("GET", f"/api/orders/{ctx['order_no']}", token=ctx["buyer_token"]))["code"] == 0 else (False, r.get("message"))
    ))

    step("buyer orders", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", "/api/orders/buyer/list", token=ctx["buyer_token"]))["code"] == 0 else (False, r.get("message"))
    ))

    step("seller orders", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", "/api/orders/seller/list", token=ctx["seller_token"]))["code"] == 0 else (False, r.get("message"))
    ))

    step("pay submit", lambda: (
        (True, "url generated") if (r := req("POST", "/api/pay/submit", {"orderNo": ctx["order_no"]}, token=ctx["buyer_token"]))["code"] == 0 and str(r.get("data", "")).startswith("http") else (False, r.get("message", "no url"))
    ))

    step("pay sync-status", lambda: (
        (True, str(r["data"])) if (r := req("GET", f"/api/pay/sync-status?orderNo={ctx['order_no']}"))["code"] == 0 else (False, r.get("message"))
    ))

    def cancel_order():
        r = req("POST", "/api/orders/create", {"goodsId": ctx["goods_id"], "quantity": 1, "payType": 1}, token=ctx["buyer_token"])
        if r["code"] != 0:
            return False, r.get("message")
        no = r["data"]["orderNo"]
        c = req("POST", "/api/orders/cancel", {"orderNo": no}, token=ctx["buyer_token"])
        return c["code"] == 0, c.get("message", no)

    step("cancel order", cancel_order)

    step("campus zone", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", "/api/goods/campus"))["code"] == 0 else (False, r.get("message"))
    ))

    step("exchange zone", lambda: (
        (True, f"count={len(r['data'])}") if (r := req("GET", "/api/goods/exchange"))["code"] == 0 else (False, r.get("message"))
    ))

    # image access
    if ctx.get("image"):
        def check_image():
            url = BASE + ctx["image"]
            urllib.request.urlopen(url, timeout=10)
            return True, url
        step("image static access", check_image)

    passed = sum(1 for _, s, _ in results if s == "PASS")
    failed = sum(1 for _, s, _ in results if s == "FAIL")
    print(f"\n=== Summary: {passed}/{len(results)} passed, {failed} failed ===")
    if failed:
        print("\nFailed:")
        for name, status, detail in results:
            if status == "FAIL":
                print(f"  - {name}: {detail}")
        sys.exit(1)


if __name__ == "__main__":
    main()

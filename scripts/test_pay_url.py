#!/usr/bin/env python3
"""Verify GET pay URL redirects to Alipay cashier."""
import json
import re
import time
import uuid
import urllib.request

BASE = "http://localhost:8080"
TS = str(int(time.time()))
PNG = bytes([
    0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
    0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
    0x08, 0x02, 0x00, 0x00, 0x00, 0x90, 0x77, 0x53, 0xDE, 0x00, 0x00, 0x00,
    0x0C, 0x49, 0x44, 0x41, 0x54, 0x08, 0xD7, 0x63, 0xF8, 0xCF, 0xC0, 0x00,
    0x00, 0x03, 0x01, 0x01, 0x00, 0x18, 0xDD, 0x8D, 0xB4, 0x00, 0x00, 0x00,
    0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82,
])


def req(method, path, body=None, token=None, multipart=None):
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
    request = urllib.request.Request(BASE + path, data=data, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main():
    seller = f"ps{TS}"
    buyer = f"pb{TS}"
    req("POST", "/api/auth/register", {"username": seller, "password": "123456", "phone": f"138{TS[-8:]}", "campus": "T"})
    req("POST", "/api/auth/register", {"username": buyer, "password": "123456", "phone": f"139{TS[-8:]}", "campus": "T"})
    st = req("POST", "/api/auth/login", {"username": seller, "password": "123456"})["data"]["token"]
    bt = req("POST", "/api/auth/login", {"username": buyer, "password": "123456"})["data"]["token"]
    img = req("POST", "/api/upload/image", multipart=("t.png", PNG, "image/png"), token=st)["data"]
    gid = req("POST", "/api/goods/create", {
        "title": "PayTest", "description": "x", "price": 0.01, "stock": 1,
        "groupName": "SEVENTEEN", "idolName": "Mingyu", "quality": "无暇",
        "tradeType": "仅出售", "deliveryMode": 1, "coverImage": img,
    }, token=st)["data"]["id"]
    order_no = req("POST", "/api/orders/create", {"goodsId": gid, "quantity": 1, "payType": 1}, token=bt)["data"]["orderNo"]
    pay = req("POST", "/api/pay/submit", {"orderNo": order_no}, token=bt)
    pay_url = pay["data"]
    print("pay_url_prefix", pay_url[:120])
    assert pay_url.startswith("http"), "expected http url"
    request = urllib.request.Request(pay_url, method="GET")
    request.add_header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    opener = urllib.request.build_opener(urllib.request.HTTPRedirectHandler())
    resp = opener.open(request, timeout=60)
    body = resp.read()
    final = resp.geturl()
    ok = "excashier" in final or "auth.htm" in final or len(body) > 10000
    print("final_url", final[:150])
    print("body_len", len(body))
    print("PASS" if ok else "FAIL")


if __name__ == "__main__":
    main()

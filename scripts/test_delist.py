#!/usr/bin/env python3
"""Verify goods stay on shelf until payment."""
import json
import time
import uuid
import urllib.request

BASE = "http://localhost:8080"
TS = str(int(time.time()))
PNG = bytes([
    0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
    0x49, 0x48, 0x44, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
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
    request = urllib.request.Request(url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))


def snapshot(goods_id, seller_token):
    mine = req("GET", "/api/goods/my", token=seller_token)["data"]
    g = next(x for x in mine if str(x["id"]) == str(goods_id))
    in_list = any(str(x["id"]) == str(goods_id) for x in req("GET", "/api/goods/list")["data"])
    detail = req("GET", f"/api/goods/detail?id={goods_id}")
    return g.get("stock"), g.get("status"), in_list, detail.get("code") == 0


def main():
    seller = f"seller_d_{TS}"
    buyer = f"buyer_d_{TS}"
    req("POST", "/api/auth/register", {
        "username": seller, "password": "123456",
        "phone": f"138{TS[-8:]}", "campus": "TestU",
    })
    req("POST", "/api/auth/register", {
        "username": buyer, "password": "123456",
        "phone": f"139{TS[-8:]}", "campus": "TestU",
    })
    seller_token = req("POST", "/api/auth/login", {"username": seller, "password": "123456"})["data"]["token"]
    buyer_token = req("POST", "/api/auth/login", {"username": buyer, "password": "123456"})["data"]["token"]

    img = req("POST", "/api/upload/image", multipart=("t.png", PNG, "image/png"), token=seller_token)["data"]
    goods = req("POST", "/api/goods/create", {
        "title": f"Delist Test {TS}", "description": "auto", "price": 0.01, "stock": 1,
        "groupName": "SEVENTEEN", "idolName": "Mingyu", "quality": "无暇",
        "tradeType": "仅出售", "deliveryMode": 1, "coverImage": img,
    }, token=seller_token)["data"]
    goods_id = goods["id"]

    order = req("POST", "/api/orders/create", {
        "goodsId": goods_id, "quantity": 1, "payType": 1,
    }, token=buyer_token)["data"]

    stock, status, in_list, detail_ok = snapshot(goods_id, seller_token)
    ok = stock == 1 and status == 1 and in_list and detail_ok
    print(f"unpaid order={order['orderNo']} stock={stock} status={status} in_list={in_list}")
    print("PASS" if ok else "FAIL")
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())

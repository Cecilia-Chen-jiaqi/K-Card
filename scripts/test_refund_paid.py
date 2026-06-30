#!/usr/bin/env python3
"""Find paid orders and test refund API."""
import json
import pymysql
import urllib.request
import urllib.error

DB = dict(host="localhost", user="root", password="123456", database="kpop_trade", charset="utf8mb4")


def api(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request("http://localhost:8080" + path, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read())


def main():
    conn = pymysql.connect(**DB)
    with conn.cursor() as cur:
        cur.execute(
            "SELECT o.order_no, o.status, o.amount, o.buyer_id, u.username "
            "FROM orders o JOIN user u ON u.id=o.buyer_id "
            "WHERE o.status IN (1,2) ORDER BY o.id DESC LIMIT 5"
        )
        paid = cur.fetchall()
    conn.close()

    print("=== Paid orders (status 1/2) ===")
    if not paid:
        print("None - complete a sandbox payment first")
        return

    for row in paid:
        print(row)

    order_no, status, amount, buyer_id, username = paid[0]
    print(f"\nTesting refund for {order_no} buyer={username}")

    # login as buyer - try username from DB
    for pwd in ("123456",):
        try:
            login = api("POST", "/api/auth/login", {"username": username, "password": pwd})
            if login.get("code") == 0:
                token = login["data"]["token"]
                break
        except Exception as e:
            print("login fail", username, e)
            token = None
    else:
        token = None

    if not token:
        print("Cannot login as buyer - skip API refund test")
        return

    sync = api("GET", f"/api/pay/sync-status?orderNo={order_no}")
    print("sync-status:", sync)

    try:
        refund = api("POST", "/api/pay/refund", {"orderNo": order_no, "reason": "自动化测试退款"}, token=token)
        print("refund:", refund)
    except urllib.error.HTTPError as e:
        print("refund HTTP", e.code, e.read().decode())


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Diagnose refund API issues."""
import json
import sys

try:
    import pymysql
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql", "-q"])
    import pymysql

DB = dict(host="localhost", user="root", password="123456", database="kpop_trade", charset="utf8mb4")

def main():
    conn = pymysql.connect(**DB)
    try:
        with conn.cursor() as cur:
            print("=== Orders (status 1/2) ===")
            cur.execute(
                "SELECT order_no, status, amount, buyer_id FROM orders WHERE status IN (1,2,5) ORDER BY id DESC LIMIT 10"
            )
            for row in cur.fetchall():
                print(row)

            print("\n=== Payment logs ===")
            cur.execute("SELECT order_no, trade_no, amount, status FROM payment_log ORDER BY id DESC LIMIT 10")
            logs = cur.fetchall()
            for row in logs:
                print(row)

            print("\n=== Refund logs ===")
            cur.execute("SELECT order_no, refund_no, trade_no, amount, status FROM refund_log ORDER BY id DESC LIMIT 10")
            for row in cur.fetchall():
                print(row)

            if not logs:
                print("\nNo payment logs - refund will fail with '未找到支付记录'")
                return

            order_no, trade_no, amount, status = logs[0]
            print(f"\n=== Test Alipay refund for order={order_no} trade={trade_no} amount={amount} ===")

    finally:
        conn.close()

    # Call backend refund if possible
    import urllib.request
    import urllib.error

    # Try login as cc or first user
    login_body = json.dumps({"username": "cc", "password": "123456"}).encode()
    req = urllib.request.Request(
        "http://localhost:8080/api/user/login",
        data=login_body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    token = None
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read())
            token = data.get("data", {}).get("token")
            print("Login:", data.get("code"), data.get("message"))
    except Exception as e:
        print("Login failed:", e)

    if not token:
        print("Skip API test - no token")
        return

    order_no = logs[0][0]
    refund_body = json.dumps({"orderNo": order_no, "reason": "诊断测试退款"}).encode()
    req = urllib.request.Request(
        "http://localhost:8080/api/pay/refund",
        data=refund_body,
        headers={"Content-Type": "application/json", "Authorization": "Bearer " + token},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            print("Refund response:", resp.read().decode())
    except urllib.error.HTTPError as e:
        print("Refund HTTP", e.code, e.read().decode())
    except Exception as e:
        print("Refund error:", e)

if __name__ == "__main__":
    main()

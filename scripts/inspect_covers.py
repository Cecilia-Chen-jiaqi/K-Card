#!/usr/bin/env python3
import os, sys
try:
    import pymysql
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql", "-q"])
    import pymysql

conn = pymysql.connect(host="localhost", user="root", password="123456", database="kpop_trade", charset="utf8mb4")
with conn.cursor() as cur:
    cur.execute("SELECT id, title, cover_image FROM goods WHERE status = 1 LIMIT 40")
    for row in cur.fetchall():
        print(row)
conn.close()

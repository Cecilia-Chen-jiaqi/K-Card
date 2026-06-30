#!/usr/bin/env python3
"""Delist goods whose cover image is missing or too small (test placeholder)."""
import os
import sys

try:
    import pymysql
except ImportError:
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql", "-q"])
    import pymysql

UPLOAD_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "backend", "upload"))
MIN_BYTES = 2048  # under 2KB treated as invalid/placeholder cover

conn = pymysql.connect(
    host="localhost", user="root", password="123456", database="kpop_trade", charset="utf8mb4"
)
to_delist = []

with conn.cursor() as cur:
    cur.execute("SELECT id, title, cover_image FROM goods WHERE status = 1")
    rows = cur.fetchall()
    for gid, title, cover in rows:
        cover = (cover or "").strip()
        reason = None
        if not cover:
            reason = "empty"
        elif cover.startswith("/upload/"):
            fpath = os.path.join(UPLOAD_DIR, cover.replace("/upload/", "").replace("\\", "/"))
            if not os.path.isfile(fpath):
                reason = "missing"
            elif os.path.getsize(fpath) < MIN_BYTES:
                reason = f"tiny({os.path.getsize(fpath)}B)"
        elif cover.startswith("data:"):
            if len(cover) < 200:
                reason = "tiny-data-uri"
        if reason:
            to_delist.append((gid, title, cover, reason))
            print(f"DELIST id={gid} reason={reason} title={title!r} cover={cover}")

    if to_delist:
        ids = [x[0] for x in to_delist]
        placeholders = ",".join(["%s"] * len(ids))
        cur.execute(
            f"UPDATE goods SET status = 0, updated_at = NOW() WHERE id IN ({placeholders})",
            ids,
        )
        conn.commit()
        print(f"\nDelisted {cur.rowcount} goods without valid photo")
    else:
        print("No goods matched delist criteria")

conn.close()

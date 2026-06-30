#!/usr/bin/env python3
"""Delete goods without cover images and obvious test listings."""
import sys

try:
    import pymysql
except ImportError:
    print("Installing pymysql...")
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "pymysql", "-q"])
    import pymysql

DB = dict(host="localhost", user="root", password="123456", database="kpop_trade", charset="utf8mb4")

SELECT_SQL = """
SELECT id, title, cover_image FROM goods
WHERE cover_image IS NULL OR TRIM(cover_image) = ''
   OR title LIKE 'Test Card%%'
ORDER BY id
"""

def main():
    conn = pymysql.connect(**DB)
    try:
        with conn.cursor() as cur:
            cur.execute(SELECT_SQL)
            rows = cur.fetchall()
            if not rows:
                print("No goods to delete.")
                return
            ids = [r[0] for r in rows]
            print("Will delete %d goods:" % len(ids))
            for r in rows:
                print("  - id=%s title=%r cover=%r" % (r[0], r[1], r[2]))

            placeholders = ",".join(["%s"] * len(ids))

            cur.execute("DELETE FROM cart WHERE goods_id IN (%s)" % placeholders, ids)
            print("Deleted cart rows:", cur.rowcount)

            cur.execute("DELETE FROM kpop_goods_detail WHERE goods_id IN (%s)" % placeholders, ids)
            print("Deleted kpop_goods_detail rows:", cur.rowcount)

            cur.execute("DELETE FROM goods WHERE id IN (%s)" % placeholders, ids)
            print("Deleted goods rows:", cur.rowcount)

        conn.commit()
        print("Done.")
    finally:
        conn.close()

if __name__ == "__main__":
    main()

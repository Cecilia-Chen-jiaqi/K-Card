import pymysql
c = pymysql.connect(host='localhost', user='root', password='123456', database='kpop_trade', charset='utf8mb4')
cur = c.cursor()
cur.execute("UPDATE orders SET status=5 WHERE order_no=%s AND status IN (1,2)", ('ORD1782702239343',))
c.commit()
print('synced rows:', cur.rowcount)
c.close()

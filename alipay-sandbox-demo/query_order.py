"""订单查询示例。"""
import sys

from alipay_client import create_alipay_client

alipay = create_alipay_client(debug=True)

# 可通过命令行传入商户订单号：python query_order.py TEST20260626120000
out_trade_no = sys.argv[1] if len(sys.argv) > 1 else input("请输入商户订单号 out_trade_no: ").strip()

result = alipay.api_alipay_trade_query(out_trade_no=out_trade_no)

print("查询结果:", result)

if result.get("code") == "10000":
    print("查询成功")
    print("  商户订单号:", result.get("out_trade_no"))
    print("  支付宝交易号:", result.get("trade_no"))
    print("  交易状态:", result.get("trade_status"))
    print("  订单金额:", result.get("total_amount"))
    print("  买家账号:", result.get("buyer_logon_id"))
else:
    print("查询失败:", result.get("sub_msg") or result.get("msg"))

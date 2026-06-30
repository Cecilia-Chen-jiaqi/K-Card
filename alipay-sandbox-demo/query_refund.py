"""退款记录查询示例。"""
import sys

from alipay_client import create_alipay_client

alipay = create_alipay_client(debug=True)

# 用法:
#   python query_refund.py <退款请求号> <trade_no>
#   python query_refund.py REFUND20260626102955 2026062622001488880510034842
if len(sys.argv) >= 3:
    out_request_no = sys.argv[1]
    trade_no = sys.argv[2]
else:
    out_request_no = input("请输入退款请求号 out_request_no: ").strip()
    trade_no = input("请输入支付宝交易号 trade_no: ").strip()

result = alipay.api_alipay_trade_fastpay_refund_query(
    out_request_no=out_request_no,
    trade_no=trade_no,
)

print("退款查询结果:", result)

if result.get("code") == "10000":
    print("查询成功")
    print("  退款请求号:", result.get("out_request_no"))
    print("  支付宝交易号:", result.get("trade_no"))
    print("  商户订单号:", result.get("out_trade_no"))
    print("  退款金额:", result.get("refund_amount"))
    print("  退款状态:", result.get("refund_status"))
    print("  退款时间:", result.get("gmt_refund_pay"))
else:
    print("查询失败:", result.get("sub_msg") or result.get("msg"))

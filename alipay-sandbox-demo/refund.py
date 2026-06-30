"""退款示例。"""
import sys
from datetime import datetime

from alipay_client import create_alipay_client

alipay = create_alipay_client(debug=True)

if len(sys.argv) >= 3:
    trade_no = sys.argv[1]
    refund_amount = sys.argv[2]
else:
    trade_no = input("请输入支付宝交易号 trade_no: ").strip()
    refund_amount = input("请输入退款金额（元）: ").strip() or "0.01"

out_request_no = "REFUND" + datetime.now().strftime("%Y%m%d%H%M%S")

result = alipay.api_alipay_trade_refund(
    out_trade_no=None,
    trade_no=trade_no,
    refund_amount=refund_amount,
    out_request_no=out_request_no,
    refund_reason="沙箱实验退款测试",
)

print("退款请求号:", out_request_no)
print("退款结果:", result)

if result.get("code") == "10000":
    print("退款成功")
    print("  退款金额:", result.get("refund_fee"))
    print("  买家账号:", result.get("buyer_logon_id"))
else:
    print("退款失败:", result.get("sub_msg") or result.get("msg"))

"""当面付条码支付示例。"""
from datetime import datetime

from alipay_client import create_alipay_client

# 沙箱买家账号付款码：登录沙箱版支付宝 APP → 首页「付款」获取
# 付款码约 18 位，有效期约 1 分钟，每次测试需重新获取
AUTH_CODE = "288940551275561977"

alipay = create_alipay_client(debug=True)

out_trade_no = "TEST" + datetime.now().strftime("%Y%m%d%H%M%S")
subject = "沙箱测试商品-信息工程学院实验"
total_amount = "0.01"

result = alipay.api_alipay_trade_pay(
    out_trade_no=out_trade_no,
    scene="bar_code",
    auth_code=AUTH_CODE,
    subject=subject,
    total_amount=total_amount,
    timeout_express="2m",
)

print("商户订单号:", out_trade_no)
print("支付结果:", result)

if result.get("code") == "10000":
    print("支付成功")
    print("  支付宝交易号 trade_no:", result.get("trade_no"))
    print("  交易状态 trade_status:", result.get("trade_status"))
    print("  实收金额 receipt_amount:", result.get("receipt_amount"))
else:
    print("支付失败或未成功")
    print("  错误码 code:", result.get("code"))
    print("  错误信息 msg:", result.get("msg"))
    print("  子错误码 sub_code:", result.get("sub_code"))
    print("  子错误信息 sub_msg:", result.get("sub_msg"))

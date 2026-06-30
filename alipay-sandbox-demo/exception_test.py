"""异常场景联调测试。"""
from datetime import datetime

from alipay_client import create_alipay_client

alipay = create_alipay_client(debug=True)


def test_invalid_auth_code():
    """无效付款码。"""
    print("\n=== 测试1：无效付款码 ===")
    result = alipay.api_alipay_trade_pay(
        out_trade_no="INVALID" + datetime.now().strftime("%Y%m%d%H%M%S"),
        scene="bar_code",
        auth_code="00000000000000000",
        subject="无效付款码测试",
        total_amount="0.01",
        timeout_express="2m",
    )
    print("返回:", result)
    print("sub_code:", result.get("sub_code"), "| sub_msg:", result.get("sub_msg"))


def test_duplicate_payment(auth_code: str, out_trade_no: str):
    """重复支付同一订单号（幂等性）。"""
    print("\n=== 测试2：重复支付同一订单号 ===")
    params = dict(
        out_trade_no=out_trade_no,
        scene="bar_code",
        auth_code=auth_code,
        subject="幂等性测试",
        total_amount="0.01",
        timeout_express="2m",
    )
    first = alipay.api_alipay_trade_pay(**params)
    print("第一次支付:", first.get("code"), first.get("trade_status"))
    second = alipay.api_alipay_trade_pay(**params)
    print("第二次支付:", second)
    print("sub_code:", second.get("sub_code"), "| sub_msg:", second.get("sub_msg"))


if __name__ == "__main__":
    test_invalid_auth_code()
    print("\n提示：幂等性测试需先成功支付一笔，再传入相同 out_trade_no 与有效 auth_code")
    print("示例: test_duplicate_payment('你的付款码', 'TEST20260626120000')")

"""支付宝沙箱客户端初始化模块。"""
from alipay import AliPay

from config import (
    ALIPAY_GATEWAY_URL,
    ALIPAY_PUBLIC_KEY,
    APP_ID,
    APP_PRIVATE_KEY,
    NOTIFY_URL,
)


def create_alipay_client(*, debug: bool = False) -> AliPay:
    """创建并返回 AliPay 客户端实例。"""
    return AliPay(
        appid=APP_ID,
        app_notify_url=NOTIFY_URL,
        app_private_key_string=APP_PRIVATE_KEY,
        alipay_public_key_string=ALIPAY_PUBLIC_KEY,
        sign_type="RSA2",
        debug=debug,
    )

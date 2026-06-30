# config.py - 沙箱环境配置（复制本文件为 config.py 并填入真实密钥）

APP_ID = "你的沙箱APPID"

ALIPAY_GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do"

# 应用私钥（从密钥工具生成，PKCS1 格式，保留换行）
APP_PRIVATE_KEY = """-----BEGIN RSA PRIVATE KEY-----
MIIE...（你的应用私钥内容）...
-----END RSA PRIVATE KEY-----"""

# 支付宝公钥（从沙箱应用页面获取）
ALIPAY_PUBLIC_KEY = """-----BEGIN PUBLIC KEY-----
MIIB...（支付宝公钥内容）...
-----END PUBLIC KEY-----"""

# 异步通知地址（开发测试可用 ngrok 等内网穿透）
NOTIFY_URL = "http://你的公网地址/notify"

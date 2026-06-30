"""异步通知接收服务（开发测试用，需配合 ngrok 等内网穿透）。"""
from flask import Flask, request

from alipay_client import create_alipay_client

app = Flask(__name__)
alipay = create_alipay_client()


@app.route("/notify", methods=["POST"])
def alipay_notify():
    data = request.form.to_dict()
    print("收到异步通知:", data)

    signature = data.pop("sign", None)
    if not alipay.verify(data, signature):
        print("验签失败")
        return "fail"

    trade_status = data.get("trade_status")
    out_trade_no = data.get("out_trade_no")
    trade_no = data.get("trade_no")
    total_amount = data.get("total_amount")

    print(f"验签成功 | 订单 {out_trade_no} | 交易号 {trade_no} | 状态 {trade_status} | 金额 {total_amount}")

    if trade_status in ("TRADE_SUCCESS", "TRADE_FINISHED"):
        # 此处处理业务逻辑（更新订单状态等），需保证幂等
        pass

    return "success"


if __name__ == "__main__":
    print("异步通知服务启动: http://127.0.0.1:5000/notify")
    print("请使用 ngrok http 5000 获取公网地址，并写入 config.py 的 NOTIFY_URL")
    app.run(host="0.0.0.0", port=5000, debug=True)

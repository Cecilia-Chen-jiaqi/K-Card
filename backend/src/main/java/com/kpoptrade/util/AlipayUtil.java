package com.kpoptrade.util;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.kpoptrade.config.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class AlipayUtil {
    @Autowired
    private AlipayProperties alipayProperties;

    private volatile AlipayClient alipayClient;

    public AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            synchronized (this) {
                if (alipayClient == null) {
                    alipayClient = new DefaultAlipayClient(
                            alipayProperties.getGateway(),
                            alipayProperties.getAppId(),
                            alipayProperties.getPrivateKey(),
                            "json", "utf-8", alipayProperties.getPublicKey(), "RSA2");
                }
            }
        }
        return alipayClient;
    }

    public boolean verifySignature(Map<String, String> params) throws AlipayApiException {
        return com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                params,
                alipayProperties.getPublicKey(),
                "utf-8",
                "RSA2");
    }

    public String buildPagePayForm(String orderNo, String subject, BigDecimal amount, String body) throws AlipayApiException {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        String safeSubject = (subject == null || subject.trim().isEmpty()) ? "K-CARD小卡交易订单" : subject.trim();
        if (safeSubject.length() > 128) {
            safeSubject = safeSubject.substring(0, 128);
        }
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        String safeBody = (body == null || body.trim().isEmpty()) ? "KPOP小卡订单支付" : body.trim();

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        String returnUrl = resolveReturnUrl(orderNo);
        if (returnUrl != null && !returnUrl.isEmpty()) {
            request.setReturnUrl(returnUrl);
        }
        String notifyUrl = resolveNotifyUrl();
        if (notifyUrl != null && !notifyUrl.isEmpty()) {
            request.setNotifyUrl(notifyUrl);
        }
        Map<String, Object> biz = new HashMap<>();
        biz.put("out_trade_no", orderNo.trim());
        biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
        biz.put("total_amount", safeAmount.toPlainString());
        biz.put("subject", safeSubject);
        biz.put("body", safeBody);
        biz.put("timeout_express", "30m");
        request.setBizContent(JSON.toJSONString(biz));
        return getAlipayClient().pageExecute(request, "POST").getBody();
    }

    /** GET 模式返回可直接跳转的收银台 URL，避免 POST 表单在部分浏览器白屏 */
    public String buildPagePayUrl(String orderNo, String subject, BigDecimal amount, String body) throws AlipayApiException {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        String safeSubject = (subject == null || subject.trim().isEmpty()) ? "K-CARD小卡交易订单" : subject.trim();
        if (safeSubject.length() > 128) {
            safeSubject = safeSubject.substring(0, 128);
        }
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        String safeBody = (body == null || body.trim().isEmpty()) ? "KPOP小卡订单支付" : body.trim();

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        String returnUrl = resolveReturnUrl(orderNo);
        if (returnUrl != null && !returnUrl.isEmpty()) {
            request.setReturnUrl(returnUrl);
        }
        String notifyUrl = resolveNotifyUrl();
        if (notifyUrl != null && !notifyUrl.isEmpty()) {
            request.setNotifyUrl(notifyUrl);
        }
        Map<String, Object> biz = new HashMap<>();
        biz.put("out_trade_no", orderNo.trim());
        biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
        biz.put("total_amount", safeAmount.toPlainString());
        biz.put("subject", safeSubject);
        biz.put("body", safeBody);
        biz.put("timeout_express", "30m");
        request.setBizContent(JSON.toJSONString(biz));
        return getAlipayClient().pageExecute(request, "GET").getBody();
    }

    /** 手机网站支付 URL，供手机扫码打开（比 PC 网页支付更适合沙箱 App 扫码） */
    public String buildWapPayUrl(String orderNo, String subject, BigDecimal amount, String body) throws AlipayApiException {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        String safeSubject = (subject == null || subject.trim().isEmpty()) ? "K-CARD小卡交易订单" : subject.trim();
        if (safeSubject.length() > 128) {
            safeSubject = safeSubject.substring(0, 128);
        }
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        String safeBody = (body == null || body.trim().isEmpty()) ? "KPOP小卡订单支付" : body.trim();

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        String returnUrl = resolveReturnUrl(orderNo);
        if (returnUrl != null && !returnUrl.isEmpty()) {
            request.setReturnUrl(returnUrl);
        }
        String notifyUrl = resolveNotifyUrl();
        if (notifyUrl != null && !notifyUrl.isEmpty()) {
            request.setNotifyUrl(notifyUrl);
        }
        Map<String, Object> biz = new HashMap<>();
        biz.put("out_trade_no", orderNo.trim());
        biz.put("product_code", "QUICK_WAP_WAY");
        biz.put("total_amount", safeAmount.toPlainString());
        biz.put("subject", safeSubject);
        biz.put("body", safeBody);
        biz.put("timeout_express", "30m");
        request.setBizContent(JSON.toJSONString(biz));
        return getAlipayClient().pageExecute(request, "GET").getBody();
    }

    /** 当面付预下单，返回 qr_code 链接，供手机沙箱 App 扫码（比 PC 收银台二维码更稳定） */
    public String buildPrecreateQrCode(String orderNo, String subject, BigDecimal amount) throws AlipayApiException {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        String safeSubject = (subject == null || subject.trim().isEmpty()) ? "K-CARD订单" : subject.trim();
        if (safeSubject.length() > 128) {
            safeSubject = safeSubject.substring(0, 128);
        }
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        String notifyUrl = resolveNotifyUrl();
        if (notifyUrl != null && !notifyUrl.isEmpty()) {
            request.setNotifyUrl(notifyUrl);
        }
        Map<String, Object> biz = new HashMap<>();
        biz.put("out_trade_no", orderNo.trim());
        biz.put("total_amount", safeAmount.toPlainString());
        biz.put("subject", safeSubject);
        biz.put("timeout_express", "30m");
        request.setBizContent(JSON.toJSONString(biz));
        AlipayTradePrecreateResponse response = getAlipayClient().execute(request);
        if (!response.isSuccess()) {
            String msg = response.getSubMsg();
            if (msg == null || msg.isEmpty()) {
                msg = response.getMsg();
            }
            if (msg == null || msg.isEmpty()) {
                msg = "code=" + response.getCode() + ", subCode=" + response.getSubCode();
            }
            throw new AlipayApiException(msg);
        }
        if (response.getQrCode() == null || response.getQrCode().trim().isEmpty()) {
            throw new AlipayApiException("支付宝未返回二维码内容");
        }
        return response.getQrCode().trim();
    }

    private String resolveReturnUrl(String orderNo) {
        if (alipayProperties.isOmitReturnUrlInSandbox()) {
            return null;
        }
        String base = alipayProperties.getSandboxReturnFallback();
        if (base == null || base.trim().isEmpty()) {
            base = alipayProperties.getReturnUrl();
        } else {
            base = base.trim();
        }
        if (base == null || base.trim().isEmpty()) {
            base = "http://localhost:3000/pay-success";
        }
        String sep = base.contains("?") ? "&" : "?";
        return base + sep + "orderNo=" + orderNo.trim();
    }

    /**
     * 否则不传 notify_url，避免网关探测 localhost 导致下单 504。
     */
    private String resolveNotifyUrl() {
        String fallback = alipayProperties.getSandboxNotifyFallback();
        if (alipayProperties.isOmitNotifyInSandbox()) {
            if (fallback != null && !fallback.trim().isEmpty()) {
                return fallback.trim();
            }
            return null;
        }
        String notifyUrl = alipayProperties.getNotifyUrl();
        if (notifyUrl == null || notifyUrl.trim().isEmpty()) {
            return notifyUrl;
        }
        String lower = notifyUrl.toLowerCase();
        if (lower.contains("localhost") || lower.contains("127.0.0.1")) {
            if (fallback != null && !fallback.trim().isEmpty()) {
                return fallback.trim();
            }
            return "https://httpbin.org/post";
        }
        return notifyUrl;
    }

    public AlipayTradeQueryResponse queryOrder(String orderNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> biz = new HashMap<>();
        biz.put("out_trade_no", orderNo);
        request.setBizContent(JSON.toJSONString(biz));
        return getAlipayClient().execute(request);
    }

    public AlipayTradeRefundResponse refund(String orderNo, String tradeNo, String refundNo, BigDecimal amount, String reason) throws AlipayApiException {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.HALF_UP);
        String safeReason = (reason == null || reason.trim().isEmpty()) ? "买家申请退款" : reason.trim();

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(orderNo.trim());
        if (tradeNo != null && !tradeNo.trim().isEmpty()) {
            model.setTradeNo(tradeNo.trim());
        }
        model.setRefundAmount(safeAmount.toPlainString());
        model.setOutRequestNo(refundNo);
        model.setRefundReason(safeReason);
        request.setBizModel(model);
        return getAlipayClient().execute(request);
    }
}

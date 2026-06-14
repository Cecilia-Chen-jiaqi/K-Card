package com.kpoptrade.util;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.kpoptrade.config.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class AlipayUtil {
    @Autowired
    private AlipayProperties alipayProperties;

    public AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                alipayProperties.getGateway(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json", "utf-8", alipayProperties.getPublicKey(), "RSA2");
    }

    public boolean verifySignature(Map<String, String> params) throws AlipayApiException {
        return com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                params,
                alipayProperties.getPublicKey(),
                "utf-8",
                "RSA2");
    }

    public String buildPagePayForm(String orderNo, String subject, BigDecimal amount, String body) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayProperties.getReturnUrl());
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setBizContent(JSON.toJSONString(new java.util.HashMap<String, Object>() {{
            put("out_trade_no", orderNo);
            put("product_code", "FAST_INSTANT_TRADE_PAY");
            put("total_amount", amount.toPlainString());
            put("subject", subject);
            put("body", body);
        }}));
        return getAlipayClient().pageExecute(request).getBody();
    }

    public AlipayTradeQueryResponse queryOrder(String orderNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(JSON.toJSONString(new java.util.HashMap<String, Object>() {{
            put("out_trade_no", orderNo);
        }}));
        return getAlipayClient().execute(request);
    }

    public AlipayTradeRefundResponse refund(String tradeNo, String refundNo, BigDecimal amount, String reason) throws AlipayApiException {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setTradeNo(tradeNo);
        model.setRefundAmount(amount.toPlainString());
        model.setOutRequestNo(refundNo);
        model.setRefundReason(reason);
        request.setBizModel(model);
        return getAlipayClient().execute(request);
    }
}

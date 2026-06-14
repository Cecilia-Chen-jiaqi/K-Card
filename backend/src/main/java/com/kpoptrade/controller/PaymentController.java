package com.kpoptrade.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.PaymentLog;
import com.kpoptrade.entity.RefundLog;
import com.kpoptrade.service.OrdersService;
import com.kpoptrade.service.log.PaymentLogService;
import com.kpoptrade.service.log.RefundLogService;
import com.kpoptrade.util.AlipayUtil;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
public class PaymentController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private AlipayUtil alipayUtil;
    @Autowired
    private PaymentLogService paymentLogService;
    @Autowired
    private RefundLogService refundLogService;

    @PostMapping("/submit")
    public R<String> submitPayment(@RequestBody Map<String, Object> request) {
        String orderNo = (String) request.get("orderNo");
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        if (order.getPayType() != 1) {
            return R.error("该订单不是支付宝支付模式");
        }
        try {
            String form = alipayUtil.buildPagePayForm(orderNo, "K-CARD小卡交易订单", order.getAmount(), "KPOP小卡订单支付");
            return R.ok(form);
        } catch (AlipayApiException e) {
            return R.error("支付宝支付表单生成失败: " + e.getErrMsg());
        }
    }

    @PostMapping("/notify")
    @Transactional
    public String notify(HttpServletRequest request) throws Exception {
        Map<String, String> params = buildParams(request);
        boolean verified = alipayUtil.verifySignature(params);
        if (!verified) {
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String totalAmount = params.get("total_amount");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "failure";
        }

        if (paymentLogService.existsByOrderNoAndTradeNo(outTradeNo, tradeNo)) {
            return "success";
        }

        Orders order = ordersService.getByOrderNo(outTradeNo);
        if (order == null) {
            return "failure";
        }
        if (order.getStatus() != 0) {
            return "success";
        }

        order.setStatus(1);
        order.setUpdatedAt(new Date());
        ordersService.updateOrder(order);

        PaymentLog log = new PaymentLog();
        log.setOrderNo(outTradeNo);
        log.setTradeNo(tradeNo);
        log.setBuyerId(order.getBuyerId());
        log.setAmount(new BigDecimal(totalAmount));
        log.setStatus(tradeStatus);
        log.setNotifyContent(JSON.toJSONString(params));
        paymentLogService.createLog(log);
        return "success";
    }

    @GetMapping("/success")
    public R<String> success() {
        return R.ok("支付成功，请等待异步回调确认。", null);
    }

    @PostMapping("/refund")
    @Transactional
    public R<String> refund(@RequestBody Map<String, Object> request) {
        String orderNo = (String) request.get("orderNo");
        String refundReason = (String) request.getOrDefault("reason", "买家申请退款");
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            return R.error("订单不能退款");
        }

        PaymentLog paymentLog = paymentLogService.lambdaQuery().eq(PaymentLog::getOrderNo, orderNo).one();
        if (paymentLog == null) {
            return R.error("未找到支付记录");
        }
        String refundNo = "REF" + System.currentTimeMillis();
        try {
            AlipayTradeRefundResponse response = alipayUtil.refund(paymentLog.getTradeNo(), refundNo, paymentLog.getAmount(), refundReason);
            RefundLog refundLog = new RefundLog();
            refundLog.setOrderNo(orderNo);
            refundLog.setRefundNo(refundNo);
            refundLog.setTradeNo(paymentLog.getTradeNo());
            refundLog.setAmount(paymentLog.getAmount());
            refundLog.setStatus(response.isSuccess() ? "SUCCESS" : "FAILED");
            refundLog.setNotifyContent(JSON.toJSONString(response));
            refundLogService.createLog(refundLog);

            if (response.isSuccess()) {
                order.setStatus(5);
                order.setUpdatedAt(new Date());
                ordersService.updateOrder(order);
                return R.ok("退款请求已提交", response.getMsg());
            }
            return R.error("退款失败: " + response.getSubMsg());
        } catch (AlipayApiException e) {
            return R.error("退款接口异常: " + e.getErrMsg());
        }
    }

    private Map<String, String> buildParams(HttpServletRequest request) throws Exception {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }
}

package com.kpoptrade.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.kpoptrade.dto.RefundOrderDto;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.PaymentLog;
import com.kpoptrade.entity.RefundLog;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrderItemService;
import com.kpoptrade.service.OrdersService;
import com.kpoptrade.service.log.PaymentLogService;
import com.kpoptrade.service.log.RefundLogService;
import com.kpoptrade.util.AlipayUtil;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pay")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private AlipayUtil alipayUtil;
    @Autowired
    private PaymentLogService paymentLogService;
    @Autowired
    private RefundLogService refundLogService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderItemService orderItemService;

    @PostMapping("/submit")
    public R<String> submitPayment(@RequestBody Map<String, Object> request) {
        String orderNo = (String) request.get("orderNo");
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        Long buyerId = LoginUserHolder.getUserId();
        if (buyerId == null || !buyerId.equals(order.getBuyerId())) {
            return R.error("无权支付此订单");
        }
        if (order.getPayType() != 1) {
            return R.error("该订单不是支付宝支付模式");
        }
        if (order.getStatus() != 0) {
            return R.error("订单状态不是待支付，请重新下单");
        }
        if (!ensureStockForOrder(order)) {
            return R.error("商品库存不足，无法支付");
        }
        String addressErr = ensureAddressForOrder(order);
        if (addressErr != null) {
            return R.error(addressErr);
        }
        try {
            String payUrl = alipayUtil.buildPagePayUrl(orderNo, "K-CARD小卡交易订单", order.getAmount(), "KPOP小卡订单支付");
            return R.ok(payUrl);
        } catch (AlipayApiException e) {
            return R.error("支付宝支付链接生成失败: " + e.getErrMsg());
        }
    }

    /** 手机沙箱扫码：生成当面付二维码内容 */
    @GetMapping("/qrcode")
    public R<Map<String, String>> qrcode(@RequestParam String orderNo) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        Long buyerId = LoginUserHolder.getUserId();
        if (buyerId == null || !buyerId.equals(order.getBuyerId())) {
            return R.error("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return R.error("订单不是待支付状态");
        }
        if (!ensureStockForOrder(order)) {
            return R.error("商品库存不足，无法支付");
        }
        String addressErr = ensureAddressForOrder(order);
        if (addressErr != null) {
            return R.error(addressErr);
        }
        try {
            String qrCode;
            String qrType;
            try {
                qrCode = alipayUtil.buildPrecreateQrCode(orderNo, "K-CARD小卡交易订单", order.getAmount());
                qrType = "precreate";
            } catch (AlipayApiException precreateEx) {
                logger.warn("当面付预下单失败，改用手机网站支付链接生成二维码: {}", precreateEx.getMessage());
                qrCode = alipayUtil.buildWapPayUrl(orderNo, "K-CARD小卡交易订单", order.getAmount(), "KPOP小卡订单支付");
                qrType = "wap";
            }
            Map<String, String> result = new HashMap<>();
            result.put("qrCode", qrCode);
            result.put("qrType", qrType);
            result.put("orderNo", orderNo);
            return R.ok(result);
        } catch (AlipayApiException e) {
            String msg = e.getErrMsg();
            if (msg == null || msg.isEmpty()) {
                msg = e.getMessage();
            }
            return R.error("生成支付二维码失败: " + (msg != null ? msg : "支付宝接口异常"));
        }
    }

    @PostMapping(value = "/notify", produces = "text/plain;charset=UTF-8")
    @Transactional
    public ResponseEntity<String> notify(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(processNotify(request));
        } catch (Exception ex) {
            logger.error("支付宝回调未捕获异常", ex);
            return ResponseEntity.ok("failure");
        }
    }

    private String processNotify(HttpServletRequest request) {
        Map<String, String> params = buildParams(request);
        if (params.isEmpty()) {
            logger.warn("支付宝回调参数为空");
            return "failure";
        }

        boolean verified;
        try {
            verified = alipayUtil.verifySignature(params);
        } catch (AlipayApiException e) {
            logger.warn("支付宝回调签名校验异常: {}", e.getMessage());
            return "failure";
        }
        if (!verified) {
            logger.warn("支付宝回调签名校验失败, out_trade_no={}", params.get("out_trade_no"));
            return "failure";
        }

        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String totalAmount = params.get("total_amount");

        if (outTradeNo == null || tradeNo == null || totalAmount == null) {
            logger.warn("支付宝回调缺少必要字段: {}", JSON.toJSONString(params));
            return "failure";
        }
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            logger.info("支付宝回调非成功状态: orderNo={}, status={}", outTradeNo, tradeStatus);
            return "failure";
        }

        if (paymentLogService.existsByOrderNoAndTradeNo(outTradeNo, tradeNo)) {
            return "success";
        }

        Orders order = ordersService.getByOrderNo(outTradeNo);
        if (order == null) {
            logger.warn("支付宝回调订单不存在: {}", outTradeNo);
            return "failure";
        }
        if (order.getStatus() != 0) {
            orderItemService.fulfillPaidOrderStock(order);
            return "success";
        }

        BigDecimal payAmount = new BigDecimal(totalAmount);
        if (!orderItemService.deductStockOnPayment(order)) {
            logger.error("支付成功但库存扣减失败: orderNo={}", outTradeNo);
            return "failure";
        }
        if (!ordersService.markPaid(outTradeNo, tradeNo, payAmount)) {
            logger.warn("支付宝回调更新订单失败: orderNo={}", outTradeNo);
            return "failure";
        }

        PaymentLog log = new PaymentLog();
        log.setOrderNo(outTradeNo);
        log.setTradeNo(tradeNo);
        log.setBuyerId(order.getBuyerId());
        log.setAmount(payAmount);
        log.setStatus(tradeStatus);
        log.setNotifyContent(JSON.toJSONString(params));
        paymentLogService.createLog(log);

        logger.info("支付宝回调处理成功: orderNo={}, tradeNo={}", outTradeNo, tradeNo);
        return "success";
    }

    @GetMapping("/success")
    public R<String> success() {
        return R.ok("支付成功，请等待异步回调确认。", null);
    }

    /** 手机沙箱支付同步回跳页（公网 HTTPS，供扫码支付完成后打开） */
    @GetMapping(value = "/return", produces = "text/html;charset=UTF-8")
    public String payReturn(@RequestParam(required = false) String orderNo,
                            @RequestParam(required = false) String out_trade_no) {
        String resolved = orderNo != null && !orderNo.trim().isEmpty() ? orderNo.trim() : out_trade_no;
        if (resolved == null) {
            resolved = "";
        }
        return "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\"/>"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>"
                + "<title>支付结果 - K-CARD</title>"
                + "<style>"
                + "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;margin:0;padding:24px 16px;"
                + "background:linear-gradient(180deg,#f0f7ff,#fff);color:#333;min-height:100vh;box-sizing:border-box}"
                + ".box{max-width:420px;margin:0 auto;padding:28px 22px;background:#fff;border-radius:16px;"
                + "box-shadow:0 8px 24px rgba(74,144,226,.12);text-align:center}"
                + "h2{color:#4A90E2;margin:0 0 8px;font-size:22px}.icon{font-size:48px;margin-bottom:8px}"
                + ".tip{color:#666;font-size:14px;line-height:1.7;margin:10px 0 0}"
                + ".ok{color:#52c41a;font-weight:600}.warn{color:#faad14}.order{font-size:13px;color:#888;word-break:break-all}"
                + "</style></head><body>"
                + "<div class=\"box\"><div class=\"icon\">✅</div><h2>支付已提交</h2>"
                + (resolved.isEmpty() ? "<p class=\"warn\">未获取到订单号</p>" : "<p class=\"order\">订单号：" + resolved + "</p>")
                + "<p id=\"status\" class=\"tip ok\">若本页空白或无法打开，属沙箱正常现象</p>"
                + "<p class=\"tip\">请<strong>关闭本页</strong>，回到<strong>电脑浏览器</strong>的结算页，系统会自动检测支付结果。</p>"
                + "<p class=\"tip\">也可在电脑端点击「我已支付，同步状态」。</p></div>"
                + "<script>"
                + "var orderNo=" + JSON.toJSONString(resolved) + ";"
                + "if(orderNo){fetch('/api/pay/sync-status?orderNo='+encodeURIComponent(orderNo))"
                + ".then(function(r){return r.json()}).then(function(d){"
                + "var el=document.getElementById('status');"
                + "if(d.code===0&&d.data&&d.data.paid){el.textContent='支付已确认，请返回电脑查看订单';el.className='tip ok';}"
                + "}).catch(function(){});}"
                + "</script></body></html>";
    }

    /** 主动同步支付宝订单状态（沙箱/本地开发时异步通知不可达时使用） */
    @GetMapping("/sync-status")
    @Transactional
    public R<Map<String, Object>> syncStatus(@RequestParam String orderNo) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("localStatus", order.getStatus());
        if (order.getStatus() != 0) {
            if (order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3) {
                orderItemService.fulfillPaidOrderStock(order);
            }
            result.put("paid", true);
            return R.ok(result);
        }
        try {
            AlipayTradeQueryResponse response = alipayUtil.queryOrder(orderNo);
            result.put("alipayCode", response.getCode());
            result.put("tradeStatus", response.getTradeStatus());
            if (response.isSuccess()
                    && ("TRADE_SUCCESS".equals(response.getTradeStatus())
                    || "TRADE_FINISHED".equals(response.getTradeStatus()))) {
                String tradeNo = response.getTradeNo();
                if (!paymentLogService.existsByOrderNoAndTradeNo(orderNo, tradeNo)) {
                    if (!orderItemService.deductStockOnPayment(order)) {
                        return R.error("商品库存不足，支付无法完成，请联系客服");
                    }
                    if (!ordersService.markPaid(orderNo, tradeNo, order.getAmount())) {
                        return R.error("订单状态更新失败");
                    }
                    PaymentLog log = new PaymentLog();
                    log.setOrderNo(orderNo);
                    log.setTradeNo(tradeNo);
                    log.setBuyerId(order.getBuyerId());
                    log.setAmount(order.getAmount());
                    log.setStatus(response.getTradeStatus());
                    log.setNotifyContent(JSON.toJSONString(response));
                    paymentLogService.createLog(log);
                } else {
                    completePaymentFulfillment(order);
                }
                order = ordersService.getByOrderNo(orderNo);
                result.put("paid", true);
                result.put("localStatus", order != null ? order.getStatus() : 1);
            } else {
                result.put("paid", false);
            }
            return R.ok(result);
        } catch (AlipayApiException e) {
            return R.error("查询支付宝失败: " + e.getErrMsg());
        }
    }

    @PostMapping("/refund")
    @Transactional
    public R<String> refund(@RequestBody RefundOrderDto request) {
        String orderNo = request.getOrderNo();
        String refundReason = request.getReason() != null && !request.getReason().trim().isEmpty()
                ? request.getReason().trim()
                : "买家申请退款";
        List<Long> orderItemIds = request.getOrderItemIds();
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return R.error("订单号不能为空");
        }

        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }

        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        if (!userId.equals(order.getBuyerId())) {
            return R.error("无权操作此订单");
        }
        if (order.getStatus() != null && order.getStatus() == 5) {
            return R.error("订单已退款");
        }
        if (order.getStatus() == null || order.getStatus() == 0 || order.getStatus() == 3 || order.getStatus() == 4) {
            return R.error("当前订单状态不支持退款");
        }

        List<PaymentLog> paymentLogs = paymentLogService.lambdaQuery()
                .eq(PaymentLog::getOrderNo, orderNo)
                .orderByDesc(PaymentLog::getId)
                .list();
        PaymentLog paymentLog = paymentLogs.isEmpty() ? null : paymentLogs.get(0);
        if (paymentLog == null) {
            return R.error("未找到支付记录，请先在订单页同步支付状态");
        }

        BigDecimal alreadyRefunded = refundLogService.sumSuccessfulRefundAmount(orderNo);
        BigDecimal orderAmount = order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount;
        try {
            if (orderItemIds == null || orderItemIds.isEmpty()) {
                refundAmount = orderAmount.subtract(alreadyRefunded);
                if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    return R.error("订单已无可退金额");
                }
            } else {
                refundAmount = orderItemService.sumRefundableAmount(order, orderItemIds);
                if (alreadyRefunded.add(refundAmount).compareTo(orderAmount) > 0) {
                    return R.error("退款金额超出订单可退总额");
                }
            }
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }

        try {
            AlipayTradeQueryResponse query = alipayUtil.queryOrder(orderNo);
            if (query.isSuccess()) {
                String tradeStatus = query.getTradeStatus();
                if ("TRADE_CLOSED".equals(tradeStatus) && order.getStatus() != 5) {
                    finalizeRefund(order, orderItemIds);
                    return R.ok("订单已退款，状态已同步", null);
                }
            }
        } catch (AlipayApiException e) {
            logger.warn("退款前查询支付宝订单失败 orderNo={}: {}", orderNo, e.getErrMsg());
        }

        String refundNo = "REF" + System.currentTimeMillis();
        try {
            AlipayTradeRefundResponse response = alipayUtil.refund(
                    orderNo,
                    paymentLog.getTradeNo(),
                    refundNo,
                    refundAmount,
                    refundReason);

            RefundLog refundLog = new RefundLog();
            refundLog.setOrderNo(orderNo);
            refundLog.setRefundNo(refundNo);
            refundLog.setTradeNo(paymentLog.getTradeNo());
            refundLog.setAmount(refundAmount);
            refundLog.setStatus(response.isSuccess() ? "SUCCESS" : "FAILED");
            refundLog.setNotifyContent(JSON.toJSONString(response));
            refundLogService.createLog(refundLog);

            if (response.isSuccess()) {
                finalizeRefund(order, orderItemIds);
                return R.ok("退款成功", response.getMsg());
            }

            String subCode = response.getSubCode();
            if ("ACQ.TRADE_HAS_CLOSE".equals(subCode)) {
                finalizeRefund(order, orderItemIds);
                return R.ok("订单已退款，状态已同步", null);
            }

            return R.error(formatRefundError(response));
        } catch (AlipayApiException e) {
            logger.error("退款支付宝异常 orderNo={}", orderNo, e);
            return R.error("退款请求失败: " + resolveAlipayError(e));
        } catch (Exception e) {
            logger.error("退款处理异常 orderNo={}", orderNo, e);
            return R.error("退款处理失败: " + e.getMessage());
        }
    }

    private void finalizeRefund(Orders order, List<Long> orderItemIds) {
        String orderNo = order.getOrderNo();
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            orderItemService.markItemsRefunded(orderNo);
            orderItemService.restoreStockOnRefund(order);
            ordersService.markRefunded(orderNo);
            return;
        }
        orderItemService.markItemsRefunded(orderNo, orderItemIds);
        orderItemService.restoreStockForItems(order, orderItemIds);
        orderItemService.syncOrderStatusFromItems(order);
    }

    private boolean ensureStockForOrder(Orders order) {
        return orderItemService.ensureStockForOrder(order);
    }

    private String ensureAddressForOrder(Orders order) {
        if (!orderItemService.needsMailAddress(order)) {
            return null;
        }
        if (order.getAddressId() == null) {
            return "请先选择收货地址";
        }
        return null;
    }

    private void completePaymentFulfillment(Orders order) {
        if (order == null) {
            return;
        }
        if (order.getStatus() == 0) {
            if (!orderItemService.deductStockOnPayment(order)) {
                logger.error("补全支付履约失败(扣库存): orderNo={}", order.getOrderNo());
                return;
            }
            ordersService.markPaid(order.getOrderNo(), null, order.getAmount());
        } else if (order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3) {
            orderItemService.fulfillPaidOrderStock(order);
        }
    }

    private String formatRefundError(AlipayTradeRefundResponse response) {
        if (response.getSubMsg() != null && !response.getSubMsg().isEmpty()) {
            return "退款失败: " + response.getSubMsg();
        }
        if (response.getMsg() != null && !response.getMsg().isEmpty()) {
            return "退款失败: " + response.getMsg();
        }
        return "退款失败，请稍后重试";
    }

    private String resolveAlipayError(AlipayApiException e) {
        if (e.getErrMsg() != null && !e.getErrMsg().isEmpty()) {
            return e.getErrMsg();
        }
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            return e.getMessage();
        }
        return "支付宝服务暂时不可用";
    }

    private Map<String, String> buildParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            if (values == null || values.length == 0) {
                continue;
            }
            params.put(name, values[0]);
        }
        return params;
    }
}

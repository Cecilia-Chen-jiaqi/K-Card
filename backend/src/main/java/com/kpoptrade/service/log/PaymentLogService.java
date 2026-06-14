package com.kpoptrade.service.log;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kpoptrade.entity.PaymentLog;

public interface PaymentLogService extends IService<PaymentLog> {
    boolean existsByOrderNoAndTradeNo(String orderNo, String tradeNo);
    void createLog(PaymentLog paymentLog);
}

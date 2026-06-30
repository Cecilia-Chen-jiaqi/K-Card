package com.kpoptrade.service.log;

import com.kpoptrade.entity.RefundLog;

import java.math.BigDecimal;

public interface RefundLogService {
    void createLog(RefundLog refundLog);

    BigDecimal sumSuccessfulRefundAmount(String orderNo);
}

package com.kpoptrade.service.log;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.PaymentLog;
import com.kpoptrade.mapper.PaymentLogMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentLogServiceImpl extends ServiceImpl<PaymentLogMapper, PaymentLog> implements PaymentLogService {
    @Override
    public boolean existsByOrderNoAndTradeNo(String orderNo, String tradeNo) {
        Map<String, Object> query = new HashMap<>();
        query.put("order_no", orderNo);
        query.put("trade_no", tradeNo);
        return this.count(new QueryWrapper<PaymentLog>().allEq(query)) > 0;
    }

    @Override
    public void createLog(PaymentLog paymentLog) {
        save(paymentLog);
    }
}

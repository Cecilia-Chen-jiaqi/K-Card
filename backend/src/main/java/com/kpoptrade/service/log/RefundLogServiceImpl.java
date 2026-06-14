package com.kpoptrade.service.log;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.RefundLog;
import com.kpoptrade.mapper.RefundLogMapper;
import org.springframework.stereotype.Service;

@Service
public class RefundLogServiceImpl extends ServiceImpl<RefundLogMapper, RefundLog> implements RefundLogService {
    @Override
    public void createLog(RefundLog refundLog) {
        save(refundLog);
    }
}

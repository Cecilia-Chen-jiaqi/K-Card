package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("refund_log")
public class RefundLog {
    @TableId
    private Long id;
    private String orderNo;
    private String refundNo;
    private String tradeNo;
    private BigDecimal amount;
    private String status;
    private String notifyContent;
}

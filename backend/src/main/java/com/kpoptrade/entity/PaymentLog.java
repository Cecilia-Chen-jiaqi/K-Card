package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("payment_log")
public class PaymentLog {
    @TableId
    private Long id;
    private String orderNo;
    private String tradeNo;
    private Long buyerId;
    private BigDecimal amount;
    private String status;
    private String notifyContent;
}

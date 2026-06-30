package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long goodsId;
    private Long sellerId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    /** 0待支付 1已支付 2已发货 3已完成 5已退款 */
    private Integer status;
    private String expressCompany;
    private String trackingNo;
    private String logisticsNote;
    private Date shippedAt;
    private Date createdAt;
    private Date updatedAt;
}

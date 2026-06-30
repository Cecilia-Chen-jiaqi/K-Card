package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class Orders {
    @TableId
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long goodsId;
    private Long addressId;
    private Integer quantity;
    private BigDecimal amount;
    private Integer payType;
    private Integer status;
    private Integer isReserved;
    private Integer itemCount;
    private String expressCompany;
    private String trackingNo;
    private String logisticsNote;
    private Date shippedAt;
    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
}

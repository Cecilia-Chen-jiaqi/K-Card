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
    private Integer quantity;
    private BigDecimal amount;
    private Integer payType;
    private Integer status;
    private Integer isReserved;
    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
}

package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("goods")
public class Goods {
    @TableId
    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private java.math.BigDecimal price;
    private Integer stock;
    private String groupName;
    private String idolName;
    private String quality;
    private String tradeType;
    private Integer reserveSupport;
    private Integer deliveryMode;
    private String coverImage;
    private String defectImages;
    private Integer status;
}

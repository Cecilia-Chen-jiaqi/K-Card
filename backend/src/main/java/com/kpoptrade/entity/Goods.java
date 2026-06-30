package com.kpoptrade.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

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
    /** 小卡类型：专辑卡/特典卡/签售卡/幸运卡/周边卡等 */
    private String cardType;
    /** 回归时期/专辑名 */
    private String albumEra;
    private Integer status;
    private String rejectReason;
    private Date reviewedAt;
    private Long reviewedBy;
    private Date createdAt;
    private Date updatedAt;
}

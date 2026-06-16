package com.kpoptrade.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsCreateDto {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String groupName;
    private String idolName;
    private String quality;
    private String tradeType;
    private Integer reserveSupport;
    private Integer deliveryMode;
    private String coverImage;
    private String defectImages;
    private String cardBundle;
    private String exchangeInfo;
    private String reserveDeadline;
    private String extraInfo;
}

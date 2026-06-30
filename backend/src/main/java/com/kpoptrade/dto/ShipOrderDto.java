package com.kpoptrade.dto;

import lombok.Data;

@Data
public class ShipOrderDto {
    private String orderNo;
    private String expressCompany;
    private String trackingNo;
    private String logisticsNote;
}

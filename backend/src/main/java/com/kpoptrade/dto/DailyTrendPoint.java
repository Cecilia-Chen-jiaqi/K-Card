package com.kpoptrade.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyTrendPoint {
    private String date;
    private long newUsers;
    private long newOrders;
    private long newGoods;
    private BigDecimal gmv;
}

package com.kpoptrade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AdminStatsDto {
    private long userCount;
    private long goodsPending;
    private long goodsOnSale;
    private long goodsRejected;
    private long orderCount;
    private long paidOrderCount;
    private BigDecimal totalGmv;
    private long todayNewUsers;
    private long todayNewGoods;
    private long todayNewOrders;
    private Map<String, Long> ordersByStatus;
    private Map<String, Long> goodsByStatus;
    /** 近 7 日每日新增与成交额 */
    private List<DailyTrendPoint> trend7d;
}

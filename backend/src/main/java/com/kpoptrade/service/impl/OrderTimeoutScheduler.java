package com.kpoptrade.service.impl;

import com.kpoptrade.entity.Orders;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class OrderTimeoutScheduler {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private GoodsService goodsService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void closeExpiredOrders() {
        List<Orders> expiredOrders = ordersService.findPendingReservationOrders();
        for (Orders order : expiredOrders) {
            order.setStatus(4);
            order.setClosedAt(new Date());
            order.setUpdatedAt(new Date());
            ordersService.updateOrder(order);
            goodsService.increaseStock(order.getGoodsId(), order.getQuantity());
        }
    }
}

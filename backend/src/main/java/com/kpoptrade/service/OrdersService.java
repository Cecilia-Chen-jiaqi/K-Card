package com.kpoptrade.service;

import com.kpoptrade.entity.Orders;

import java.math.BigDecimal;
import java.util.List;

public interface OrdersService {
    Orders createOrder(Orders order);
    Orders getByOrderNo(String orderNo);
    boolean updateOrder(Orders order);
    List<Orders> findPendingReservationOrders();
    boolean closeOrder(Orders order);
    boolean markPaid(String orderNo, String tradeNo, BigDecimal amount);
    List<Orders> listByBuyer(Long buyerId);
    List<Orders> listBySeller(Long sellerId);
    boolean shipOrder(String orderNo);
    boolean completeOrder(String orderNo);
    boolean cancelOrder(String orderNo);
}

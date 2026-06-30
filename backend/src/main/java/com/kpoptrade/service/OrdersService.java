package com.kpoptrade.service;

import com.kpoptrade.entity.OrderItem;
import com.kpoptrade.entity.Orders;

import java.math.BigDecimal;
import java.util.List;

public interface OrdersService {
    Orders createOrder(Orders order);
    Orders createOrderWithItems(Orders order, List<OrderItem> items);
    Orders getByOrderNo(String orderNo);
    boolean updateOrder(Orders order);
    List<Orders> findPendingReservationOrders();
    boolean closeOrder(Orders order);
    boolean markPaid(String orderNo, String tradeNo, BigDecimal amount);

    boolean markRefunded(String orderNo);
    List<Orders> listByBuyer(Long buyerId);
    List<Orders> listBySeller(Long sellerId);
    boolean shipOrder(String orderNo, Long sellerId, String expressCompany, String trackingNo, String logisticsNote);
    boolean completeOrder(String orderNo);

    boolean completeOrder(String orderNo, List<Long> orderItemIds);
    boolean cancelOrder(String orderNo);

    boolean bindAddress(String orderNo, Long buyerId, Long addressId);
}

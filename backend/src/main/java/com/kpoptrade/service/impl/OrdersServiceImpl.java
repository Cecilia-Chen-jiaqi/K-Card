package com.kpoptrade.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kpoptrade.entity.OrderItem;

import com.kpoptrade.entity.Orders;

import com.kpoptrade.mapper.OrdersMapper;

import com.kpoptrade.service.OrderItemService;

import com.kpoptrade.service.OrdersService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Lazy;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.math.BigDecimal;

import java.util.ArrayList;

import java.util.Comparator;

import java.util.Date;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



@Service

public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired

    @Lazy

    private OrderItemService orderItemService;



    @Override

    @Transactional

    public Orders createOrder(Orders order) {

        if (order.getItemCount() == null) {

            order.setItemCount(1);

        }

        order.setStatus(0);

        order.setOrderNo("ORD" + System.currentTimeMillis());

        order.setCreatedAt(new Date());

        order.setUpdatedAt(new Date());

        save(order);

        return order;

    }



    @Override

    @Transactional

    public Orders createOrderWithItems(Orders order, List<OrderItem> items) {

        order.setItemCount(items != null ? items.size() : 1);

        Orders saved = createOrder(order);

        if (items != null && !items.isEmpty()) {

            orderItemService.saveItems(saved, items);

        }

        return saved;

    }



    @Override

    public Orders getByOrderNo(String orderNo) {

        return lambdaQuery().eq(Orders::getOrderNo, orderNo).one();

    }



    @Override

    @Transactional

    public boolean updateOrder(Orders order) {

        order.setUpdatedAt(new Date());

        return updateById(order);

    }



    @Override

    public List<Orders> findPendingReservationOrders() {

        return lambdaQuery().eq(Orders::getStatus, 0).isNotNull(Orders::getCreatedAt).lt(Orders::getCreatedAt, new Date(System.currentTimeMillis() - 3L * 24 * 3600 * 1000)).list();

    }



    @Override

    @Transactional

    public boolean closeOrder(Orders order) {

        order.setStatus(4);

        order.setClosedAt(new Date());

        order.setUpdatedAt(new Date());

        return updateById(order);

    }



    @Override

    @Transactional

    public boolean markPaid(String orderNo, String tradeNo, BigDecimal amount) {

        Orders order = getByOrderNo(orderNo);

        if (order == null || order.getStatus() != 0) {

            return false;

        }

        order.setStatus(1);

        order.setUpdatedAt(new Date());

        return updateById(order);

    }



    @Override

    @Transactional

    public boolean markRefunded(String orderNo) {

        Orders order = getByOrderNo(orderNo);

        if (order == null) {

            return false;

        }

        if (order.getStatus() != null && order.getStatus() == 5) {

            return true;

        }

        order.setStatus(5);

        order.setUpdatedAt(new Date());

        return updateById(order);

    }



    @Override

    public List<Orders> listByBuyer(Long buyerId) {

        return lambdaQuery().eq(Orders::getBuyerId, buyerId).orderByDesc(Orders::getCreatedAt).list();

    }



    @Override

    public List<Orders> listBySeller(Long sellerId) {

        Map<Long, Orders> merged = new LinkedHashMap<>();

        lambdaQuery().eq(Orders::getSellerId, sellerId).orderByDesc(Orders::getCreatedAt).list()

                .forEach(order -> merged.put(order.getId(), order));

        for (Long orderId : orderItemService.findOrderIdsBySeller(sellerId)) {

            Orders order = getById(orderId);

            if (order != null) {

                merged.putIfAbsent(order.getId(), order);

            }

        }

        return merged.values().stream()

                .sorted(Comparator.comparing(Orders::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))

                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

    }



    @Override

    @Transactional

    public boolean shipOrder(String orderNo, Long sellerId, String expressCompany, String trackingNo, String logisticsNote) {

        return orderItemService.shipSellerItems(orderNo, sellerId, expressCompany, trackingNo, logisticsNote);

    }



    @Override

    @Transactional

    public boolean completeOrder(String orderNo) {
        return completeOrder(orderNo, null);
    }

    @Override
    @Transactional
    public boolean completeOrder(String orderNo, List<Long> orderItemIds) {
        Orders order = getByOrderNo(orderNo);
        if (order == null || (order.getStatus() != 2 && order.getStatus() != 1)) {
            return false;
        }
        orderItemService.ensureLegacyMigrated(order);
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            orderItemService.markItemsCompleted(orderNo);
            orderItemService.syncOrderStatusFromItems(order);
            return true;
        }
        java.util.Set<Long> idSet = new java.util.HashSet<>(orderItemIds);
        for (com.kpoptrade.entity.OrderItem item : orderItemService.listByOrderNo(orderNo)) {
            if (!idSet.contains(item.getId())) {
                continue;
            }
            if (item.getStatus() != null && item.getStatus() == 5) {
                return false;
            }
            if (item.getStatus() == null || item.getStatus() < 2) {
                return false;
            }
        }
        orderItemService.markItemsCompleted(orderNo, orderItemIds);
        orderItemService.syncOrderStatusFromItems(order);
        return true;
    }



    @Override

    @Transactional

    public boolean cancelOrder(String orderNo) {

        Orders order = getByOrderNo(orderNo);

        if (order == null || order.getStatus() != 0) {

            return false;

        }

        order.setStatus(4);

        order.setClosedAt(new Date());

        order.setUpdatedAt(new Date());

        return updateById(order);

    }



    @Override

    @Transactional

    public boolean bindAddress(String orderNo, Long buyerId, Long addressId) {

        Orders order = getByOrderNo(orderNo);

        if (order == null || !buyerId.equals(order.getBuyerId())) {

            return false;

        }

        if (order.getStatus() != 0) {

            return false;

        }

        order.setAddressId(addressId);

        order.setUpdatedAt(new Date());

        return updateById(order);

    }

}



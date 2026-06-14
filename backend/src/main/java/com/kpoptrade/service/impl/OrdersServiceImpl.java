package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.mapper.OrdersMapper;
import com.kpoptrade.service.OrdersService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Override
    @Transactional
    public Orders createOrder(Orders order) {
        order.setStatus(0);
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        save(order);
        return order;
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
}

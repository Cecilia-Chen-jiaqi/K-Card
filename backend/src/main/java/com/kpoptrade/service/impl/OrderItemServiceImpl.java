package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.OrderItem;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.mapper.OrderItemMapper;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrderItemService;
import com.kpoptrade.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    @Lazy
    private OrdersService ordersService;

    @Override
    public List<OrderItem> listByOrderNo(String orderNo) {
        return lambdaQuery().eq(OrderItem::getOrderNo, orderNo).orderByAsc(OrderItem::getId).list();
    }

    @Override
    public List<OrderItem> listByOrderNoAndSeller(String orderNo, Long sellerId) {
        return lambdaQuery()
                .eq(OrderItem::getOrderNo, orderNo)
                .eq(OrderItem::getSellerId, sellerId)
                .orderByAsc(OrderItem::getId)
                .list();
    }

    @Override
    public List<OrderItem> listBySellerId(Long sellerId) {
        return lambdaQuery().eq(OrderItem::getSellerId, sellerId).orderByDesc(OrderItem::getId).list();
    }

    @Override
    @Transactional
    public void saveItems(Orders order, List<OrderItem> items) {
        Date now = new Date();
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setStatus(0);
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
        }
        saveBatch(items);
    }

    @Override
    @Transactional
    public void ensureLegacyMigrated(Orders order) {
        if (order == null || order.getGoodsId() == null) {
            return;
        }
        if (!listByOrderNo(order.getOrderNo()).isEmpty()) {
            return;
        }
        Goods goods = goodsService.getByIdIncludeAll(order.getGoodsId());
        if (goods == null) {
            return;
        }
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        item.setGoodsId(order.getGoodsId());
        item.setSellerId(order.getSellerId());
        item.setQuantity(order.getQuantity() != null ? order.getQuantity() : 1);
        item.setUnitPrice(goods.getPrice());
        item.setAmount(order.getAmount());
        item.setStatus(mapOrderStatusToItem(order.getStatus()));
        item.setExpressCompany(order.getExpressCompany());
        item.setTrackingNo(order.getTrackingNo());
        item.setLogisticsNote(order.getLogisticsNote());
        item.setShippedAt(order.getShippedAt());
        item.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt() : new Date());
        item.setUpdatedAt(new Date());
        save(item);
        if (order.getItemCount() == null || order.getItemCount() < 1) {
            order.setItemCount(1);
            ordersService.updateOrder(order);
        }
    }

    @Override
    public boolean userCanAccessOrder(Long userId, Orders order) {
        if (userId == null || order == null) {
            return false;
        }
        if (userId.equals(order.getBuyerId())) {
            return true;
        }
        if (order.getSellerId() != null && userId.equals(order.getSellerId())) {
            return true;
        }
        ensureLegacyMigrated(order);
        return listByOrderNo(order.getOrderNo()).stream()
                .anyMatch(item -> userId.equals(item.getSellerId()));
    }

    @Override
    public boolean isSellerOfOrder(Long sellerId, Orders order) {
        if (sellerId == null || order == null) {
            return false;
        }
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            return items.stream().anyMatch(item -> sellerId.equals(item.getSellerId()));
        }
        return sellerId.equals(order.getSellerId());
    }

    @Override
    public int countItems(Orders order) {
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            return items.size();
        }
        return order.getGoodsId() != null ? 1 : 0;
    }

    @Override
    public int totalQuantity(Orders order) {
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            return items.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
        }
        return order.getQuantity() != null ? order.getQuantity() : 0;
    }

    @Override
    public boolean needsMailAddress(Orders order) {
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            for (OrderItem item : items) {
                Goods goods = goodsService.getByIdIncludeAll(item.getGoodsId());
                if (goods != null && (goods.getDeliveryMode() == null || goods.getDeliveryMode() != 2)) {
                    return true;
                }
            }
            return false;
        }
        Goods goods = goodsService.getByIdIncludeAll(order.getGoodsId());
        return goods != null && (goods.getDeliveryMode() == null || goods.getDeliveryMode() != 2);
    }

    @Override
    public boolean ensureStockForOrder(Orders order) {
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            for (OrderItem item : items) {
                if (!hasStock(item.getGoodsId(), item.getQuantity())) {
                    return false;
                }
            }
            return true;
        }
        return hasStock(order.getGoodsId(), order.getQuantity());
    }

    @Override
    @Transactional
    public boolean deductStockOnPayment(Orders order) {
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            for (OrderItem item : items) {
                if (!goodsService.reduceStock(item.getGoodsId(), item.getQuantity())) {
                    return false;
                }
                goodsService.delistIfSoldOut(item.getGoodsId());
            }
            markItemsPaid(order.getOrderNo());
            return true;
        }
        if (!goodsService.reduceStock(order.getGoodsId(), order.getQuantity())) {
            return false;
        }
        goodsService.delistIfSoldOut(order.getGoodsId());
        return true;
    }

    @Override
    @Transactional
    public void fulfillPaidOrderStock(Orders order) {
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            for (OrderItem item : items) {
                goodsService.fulfillPaidOrderStock(item.getGoodsId(), item.getQuantity());
            }
            markItemsPaid(order.getOrderNo());
            return;
        }
        goodsService.fulfillPaidOrderStock(order.getGoodsId(), order.getQuantity());
    }

    @Override
    @Transactional
    public void restoreStockOnRefund(Orders order) {
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            for (OrderItem item : items) {
                if (item.getStatus() != null && item.getStatus() == 5) {
                    continue;
                }
                goodsService.increaseStock(item.getGoodsId(), item.getQuantity());
            }
            markItemsRefunded(order.getOrderNo());
            return;
        }
        goodsService.increaseStock(order.getGoodsId(), order.getQuantity());
    }

    @Override
    @Transactional
    public boolean shipSellerItems(String orderNo, Long sellerId, String expressCompany, String trackingNo, String logisticsNote) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null || order.getStatus() == null || order.getStatus() != 1) {
            return false;
        }
        ensureLegacyMigrated(order);
        List<OrderItem> items = listByOrderNoAndSeller(orderNo, sellerId);
        if (items.isEmpty()) {
            if (!sellerId.equals(order.getSellerId())) {
                return false;
            }
            order.setExpressCompany(expressCompany);
            order.setTrackingNo(trackingNo);
            order.setLogisticsNote(logisticsNote);
            order.setShippedAt(new Date());
            order.setStatus(2);
            order.setUpdatedAt(new Date());
            return ordersService.updateOrder(order);
        }

        Date now = new Date();
        for (OrderItem item : items) {
            if (item.getStatus() != null && item.getStatus() >= 2) {
                continue;
            }
            item.setExpressCompany(expressCompany);
            item.setTrackingNo(trackingNo);
            item.setLogisticsNote(logisticsNote);
            item.setShippedAt(now);
            item.setStatus(2);
            item.setUpdatedAt(now);
            updateById(item);
        }
        syncOrderStatusFromItems(order);
        return true;
    }

    @Override
    @Transactional
    public void syncOrderStatusFromItems(Orders order) {
        List<OrderItem> items = listByOrderNo(order.getOrderNo());
        if (items.isEmpty()) {
            return;
        }
        List<OrderItem> active = items.stream()
                .filter(i -> i.getStatus() == null || i.getStatus() != 5)
                .collect(Collectors.toList());
        if (active.isEmpty()) {
            order.setStatus(5);
            order.setUpdatedAt(new Date());
            ordersService.updateOrder(order);
            return;
        }
        boolean allCompleted = active.stream().allMatch(i -> i.getStatus() != null && i.getStatus() >= 3);
        boolean allShipped = active.stream().allMatch(i -> i.getStatus() != null && i.getStatus() >= 2);
        if (allCompleted) {
            order.setStatus(3);
        } else if (allShipped && order.getStatus() != null && order.getStatus() <= 2) {
            order.setStatus(2);
            OrderItem firstShipped = active.stream()
                    .filter(i -> i.getShippedAt() != null)
                    .findFirst()
                    .orElse(active.get(0));
            order.setExpressCompany(firstShipped.getExpressCompany());
            order.setTrackingNo(firstShipped.getTrackingNo());
            order.setLogisticsNote(firstShipped.getLogisticsNote());
            order.setShippedAt(firstShipped.getShippedAt());
        } else if (active.stream().anyMatch(i -> i.getStatus() != null && i.getStatus() >= 1)) {
            if (order.getStatus() == null || order.getStatus() == 0) {
                order.setStatus(1);
            }
        }
        order.setUpdatedAt(new Date());
        ordersService.updateOrder(order);
    }

    @Override
    @Transactional
    public void markItemsPaid(String orderNo) {
        List<OrderItem> items = listByOrderNo(orderNo);
        Date now = new Date();
        for (OrderItem item : items) {
            if (item.getStatus() == null || item.getStatus() == 0) {
                item.setStatus(1);
                item.setUpdatedAt(now);
                updateById(item);
            }
        }
    }

    @Override
    @Transactional
    public void markItemsRefunded(String orderNo) {
        markItemsRefunded(orderNo, null);
    }

    @Override
    @Transactional
    public void markItemsRefunded(String orderNo, List<Long> orderItemIds) {
        List<OrderItem> items = listByOrderNo(orderNo);
        Date now = new Date();
        Set<Long> idSet = orderItemIds == null || orderItemIds.isEmpty()
                ? null
                : new HashSet<>(orderItemIds);
        for (OrderItem item : items) {
            if (idSet != null && !idSet.contains(item.getId())) {
                continue;
            }
            item.setStatus(5);
            item.setUpdatedAt(now);
            updateById(item);
        }
    }

    @Override
    @Transactional
    public void markItemsCompleted(String orderNo) {
        markItemsCompleted(orderNo, null);
    }

    @Override
    @Transactional
    public void markItemsCompleted(String orderNo, List<Long> orderItemIds) {
        List<OrderItem> items = listByOrderNo(orderNo);
        Date now = new Date();
        Set<Long> idSet = orderItemIds == null || orderItemIds.isEmpty()
                ? null
                : new HashSet<>(orderItemIds);
        for (OrderItem item : items) {
            if (idSet != null && !idSet.contains(item.getId())) {
                continue;
            }
            if (item.getStatus() != null && item.getStatus() == 5) {
                continue;
            }
            if (item.getStatus() == null || item.getStatus() < 3) {
                item.setStatus(3);
                item.setUpdatedAt(now);
                updateById(item);
            }
        }
    }

    @Override
    @Transactional
    public void restoreStockForItems(Orders order, List<Long> orderItemIds) {
        ensureLegacyMigrated(order);
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            restoreStockOnRefund(order);
            return;
        }
        Set<Long> idSet = new HashSet<>(orderItemIds);
        for (OrderItem item : listByOrderNo(order.getOrderNo())) {
            if (!idSet.contains(item.getId())) {
                continue;
            }
            goodsService.increaseStock(item.getGoodsId(), item.getQuantity());
        }
    }

    @Override
    public BigDecimal sumRefundableAmount(Orders order, List<Long> orderItemIds) {
        ensureLegacyMigrated(order);
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            return order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO;
        }
        Set<Long> idSet = new HashSet<>(orderItemIds);
        BigDecimal sum = BigDecimal.ZERO;
        for (OrderItem item : listByOrderNo(order.getOrderNo())) {
            if (!idSet.contains(item.getId())) {
                continue;
            }
            if (item.getStatus() != null && item.getStatus() == 5) {
                throw new IllegalArgumentException("所选商品已退款");
            }
            if (item.getStatus() == null || (item.getStatus() != 1 && item.getStatus() != 2)) {
                throw new IllegalArgumentException("所选商品当前状态不可退款");
            }
            sum = sum.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
        }
        if (sum.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("请选择有效的退款明细");
        }
        return sum;
    }

    @Override
    public List<Long> findOrderIdsBySeller(Long sellerId) {
        Set<Long> orderIds = new HashSet<>();
        listBySellerId(sellerId).forEach(item -> orderIds.add(item.getOrderId()));
        return new ArrayList<>(orderIds);
    }

    private boolean hasStock(Long goodsId, Integer quantity) {
        Goods goods = goodsService.getByIdIncludeAll(goodsId);
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            return false;
        }
        Integer stock = goods.getStock();
        return stock != null && quantity != null && stock >= quantity;
    }

    private int mapOrderStatusToItem(Integer orderStatus) {
        if (orderStatus == null) {
            return 0;
        }
        if (orderStatus == 5) {
            return 5;
        }
        if (orderStatus >= 3) {
            return 3;
        }
        if (orderStatus >= 2) {
            return 2;
        }
        if (orderStatus >= 1) {
            return 1;
        }
        return 0;
    }
}

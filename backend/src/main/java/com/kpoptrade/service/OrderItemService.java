package com.kpoptrade.service;

import com.kpoptrade.entity.OrderItem;
import com.kpoptrade.entity.Orders;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemService {
    List<OrderItem> listByOrderNo(String orderNo);

    List<OrderItem> listByOrderNoAndSeller(String orderNo, Long sellerId);

    List<OrderItem> listBySellerId(Long sellerId);

    void saveItems(Orders order, List<OrderItem> items);

    void ensureLegacyMigrated(Orders order);

    boolean userCanAccessOrder(Long userId, Orders order);

    boolean isSellerOfOrder(Long sellerId, Orders order);

    int countItems(Orders order);

    int totalQuantity(Orders order);

    boolean needsMailAddress(Orders order);

    boolean ensureStockForOrder(Orders order);

    boolean deductStockOnPayment(Orders order);

    void fulfillPaidOrderStock(Orders order);

    void restoreStockOnRefund(Orders order);

    void restoreStockForItems(Orders order, List<Long> orderItemIds);

    boolean shipSellerItems(String orderNo, Long sellerId, String expressCompany, String trackingNo, String logisticsNote);

    void syncOrderStatusFromItems(Orders order);

    void markItemsPaid(String orderNo);

    void markItemsRefunded(String orderNo);

    void markItemsRefunded(String orderNo, List<Long> orderItemIds);

    void markItemsCompleted(String orderNo);

    void markItemsCompleted(String orderNo, List<Long> orderItemIds);

    BigDecimal sumRefundableAmount(Orders order, List<Long> orderItemIds);

    List<Long> findOrderIdsBySeller(Long sellerId);
}

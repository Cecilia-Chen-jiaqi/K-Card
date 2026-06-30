package com.kpoptrade.controller;

import com.kpoptrade.dto.CompleteOrderDto;
import com.kpoptrade.dto.OrderDetailDto;
import com.kpoptrade.dto.OrderLineDto;
import com.kpoptrade.dto.ShipOrderDto;
import com.kpoptrade.entity.Address;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.OrderItem;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.OrderItemService;
import com.kpoptrade.service.AddressService;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrdersService;
import com.kpoptrade.service.UserService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private OrderItemService orderItemService;

    @PostMapping("/create")
    @Transactional
    public R<Orders> create(@RequestBody Orders order) {
        Long buyerId = LoginUserHolder.getUserId();
        order.setBuyerId(buyerId);
        if (order.getGoodsId() == null) {
            return R.error("请选择商品");
        }
        Goods goods = goodsService.getById(order.getGoodsId());
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            return R.error("商品不存在或已下架");
        }
        try {
            goodsService.ensureNotSelfPurchase(buyerId, goods.getSellerId());
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            order.setQuantity(1);
        }
        if (goods.getStock() == null || goods.getStock() < order.getQuantity()) {
            return R.error("库存不足");
        }
        order.setGoodsId(goods.getId());
        order.setSellerId(goods.getSellerId());
        order.setAmount(goods.getPrice().multiply(new java.math.BigDecimal(order.getQuantity())));
        if (order.getPayType() == null) {
            order.setPayType(1);
        }
        order.setIsReserved(goods.getReserveSupport());
        bindDefaultAddressIfNeeded(order, goods, buyerId);

        OrderItem line = buildOrderItem(goods, order.getQuantity());
        return R.ok(ordersService.createOrderWithItems(order, java.util.Collections.singletonList(line)));
    }

    private OrderItem buildOrderItem(Goods goods, Integer quantity) {
        int qty = quantity != null && quantity > 0 ? quantity : 1;
        OrderItem item = new OrderItem();
        item.setGoodsId(goods.getId());
        item.setSellerId(goods.getSellerId());
        item.setQuantity(qty);
        item.setUnitPrice(goods.getPrice());
        item.setAmount(goods.getPrice().multiply(BigDecimal.valueOf(qty)));
        return item;
    }

    private void bindDefaultAddressIfNeeded(Orders order, Goods goods, Long buyerId) {
        if (goods.getDeliveryMode() != null && goods.getDeliveryMode() == 2) {
            return;
        }
        if (order.getAddressId() != null) {
            return;
        }
        Address defaultAddr = addressService.getDefaultByUser(buyerId);
        if (defaultAddr != null) {
            order.setAddressId(defaultAddr.getId());
        }
    }

    @PostMapping("/bind-address")
    public R<Boolean> bindAddress(@RequestBody Map<String, Object> body) {
        Long buyerId = LoginUserHolder.getUserId();
        if (buyerId == null) {
            return R.error("请先登录");
        }
        String orderNo = body.get("orderNo") != null ? body.get("orderNo").toString() : null;
        Object addressIdObj = body.get("addressId");
        if (orderNo == null || orderNo.trim().isEmpty() || addressIdObj == null) {
            return R.error("参数不完整");
        }
        Long addressId = Long.valueOf(addressIdObj.toString());
        if (!addressService.belongsToUser(buyerId, addressId)) {
            return R.error("收货地址不存在");
        }
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null || !buyerId.equals(order.getBuyerId())) {
            return R.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return R.error("当前订单无法修改收货地址");
        }
        if (!ordersService.bindAddress(orderNo, buyerId, addressId)) {
            return R.error("绑定收货地址失败");
        }
        return R.ok(true);
    }

    @GetMapping("/{orderNo}")
    public R<OrderDetailDto> get(@PathVariable String orderNo) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        Long userId = LoginUserHolder.getUserId();
        if (!orderItemService.userCanAccessOrder(userId, order)) {
            return R.error("无权查看此订单");
        }
        return R.ok(buildOrderDetail(order, userId));
    }

    @GetMapping("/buyer/list")
    public R<List<OrderDetailDto>> listBuyer() {
        Long buyerId = LoginUserHolder.getUserId();
        List<Orders> orders = ordersService.listByBuyer(buyerId);
        return R.ok(orders.stream().map(o -> buildOrderDetail(o, buyerId)).collect(Collectors.toList()));
    }

    @GetMapping("/seller/list")
    public R<List<OrderDetailDto>> listSeller() {
        Long sellerId = LoginUserHolder.getUserId();
        List<Orders> orders = ordersService.listBySeller(sellerId);
        return R.ok(orders.stream().map(o -> buildOrderDetail(o, sellerId)).collect(Collectors.toList()));
    }

    @PostMapping("/ship")
    public R<String> ship(@RequestBody ShipOrderDto request) {
        if (request.getOrderNo() == null || request.getOrderNo().trim().isEmpty()) {
            return R.error("订单号不能为空");
        }
        Orders order = ordersService.getByOrderNo(request.getOrderNo());
        if (order == null) {
            return R.error("订单不存在");
        }
        if (!orderItemService.isSellerOfOrder(LoginUserHolder.getUserId(), order)) {
            return R.error("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            return R.error("当前订单无法发货");
        }

        Long sellerId = LoginUserHolder.getUserId();
        List<OrderItem> sellerItems = orderItemService.listByOrderNoAndSeller(order.getOrderNo(), sellerId);
        Goods refGoods;
        if (!sellerItems.isEmpty()) {
            refGoods = goodsService.getByIdIncludeAll(sellerItems.get(0).getGoodsId());
        } else {
            refGoods = goodsService.getByIdIncludeAll(order.getGoodsId());
        }
        boolean campusMeetup = refGoods != null && refGoods.getDeliveryMode() != null && refGoods.getDeliveryMode() == 2;

        String expressCompany = request.getExpressCompany() != null ? request.getExpressCompany().trim() : "";
        String trackingNo = request.getTrackingNo() != null ? request.getTrackingNo().trim() : "";
        String logisticsNote = request.getLogisticsNote() != null ? request.getLogisticsNote().trim() : "";

        if (campusMeetup) {
            if (expressCompany.isEmpty()) {
                expressCompany = "校园面交";
            }
        } else {
            if (expressCompany.isEmpty()) {
                return R.error("请选择快递公司");
            }
            if (trackingNo.isEmpty()) {
                return R.error("请填写快递单号");
            }
            if (trackingNo.length() < 6) {
                return R.error("快递单号格式不正确");
            }
        }

        if (!ordersService.shipOrder(order.getOrderNo(), sellerId, expressCompany, trackingNo, logisticsNote)) {
            return R.error("发货失败");
        }
        return R.ok("发货成功");
    }

    @PostMapping("/complete")
    public R<String> complete(@RequestBody CompleteOrderDto request) {
        if (request.getOrderNo() == null || request.getOrderNo().trim().isEmpty()) {
            return R.error("订单号不能为空");
        }
        Orders order = ordersService.getByOrderNo(request.getOrderNo());
        if (order == null) {
            return R.error("订单不存在");
        }
        if (!order.getBuyerId().equals(LoginUserHolder.getUserId())) {
            return R.error("无权操作此订单");
        }
        if (order.getStatus() != 2 && order.getStatus() != 1) {
            return R.error("当前订单无法确认收货");
        }
        if (!ordersService.completeOrder(order.getOrderNo(), request.getOrderItemIds())) {
            return R.error("确认收货失败，请确认所选商品已发货");
        }
        return R.ok("交易完成");
    }

    @PostMapping("/cancel")
    public R<String> cancel(@RequestBody Orders request) {
        Orders order = ordersService.getByOrderNo(request.getOrderNo());
        if (order == null) {
            return R.error("订单不存在");
        }
        if (!order.getBuyerId().equals(LoginUserHolder.getUserId())) {
            return R.error("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return R.error("当前订单无法取消");
        }
        if (!ordersService.cancelOrder(order.getOrderNo())) {
            return R.error("取消订单失败");
        }
        return R.ok("订单已取消");
    }

    private OrderDetailDto buildOrderDetail(Orders order, Long viewerId) {
        orderItemService.ensureLegacyMigrated(order);
        OrderDetailDto dto = new OrderDetailDto();
        dto.setOrder(order);
        dto.setBuyer(userService.getById(order.getBuyerId()));
        if (order.getAddressId() != null) {
            dto.setAddress(addressService.getById(order.getAddressId()));
        }

        List<OrderLineDto> lines = new ArrayList<>();
        List<OrderItem> items = orderItemService.listByOrderNo(order.getOrderNo());
        if (!items.isEmpty()) {
            for (OrderItem item : items) {
                OrderLineDto line = new OrderLineDto();
                line.setItem(item);
                line.setGoods(goodsService.getByIdIncludeAll(item.getGoodsId()));
                line.setSeller(userService.getById(item.getSellerId()));
                lines.add(line);
            }
            dto.setItems(lines);
            dto.setMultiItem(items.size() > 1);
            OrderLineDto first = lines.get(0);
            dto.setGoods(first.getGoods());
            dto.setSeller(first.getSeller());
            if (viewerId != null && orderItemService.isSellerOfOrder(viewerId, order) && !viewerId.equals(order.getBuyerId())) {
                dto.setSellerItems(lines.stream()
                        .filter(l -> viewerId.equals(l.getItem().getSellerId()))
                        .collect(Collectors.toList()));
            }
        } else {
            dto.setGoods(goodsService.getByIdIncludeAll(order.getGoodsId()));
            dto.setSeller(userService.getById(order.getSellerId()));
            dto.setMultiItem(false);
        }
        return dto;
    }
}

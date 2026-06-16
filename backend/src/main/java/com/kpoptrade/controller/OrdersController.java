package com.kpoptrade.controller;

import com.kpoptrade.dto.OrderDetailDto;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrdersService;
import com.kpoptrade.service.UserService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PostMapping("/create")
    public R<Orders> create(@RequestBody Orders order) {
        order.setBuyerId(LoginUserHolder.getUserId());
        if (order.getGoodsId() == null) {
            return R.error("请选择商品");
        }
        Goods goods = goodsService.getById(order.getGoodsId());
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            return R.error("商品不存在或已下架");
        }
        if (order.getQuantity() == null || order.getQuantity() <= 0) {
            order.setQuantity(1);
        }
        if (goods.getStock() == null || goods.getStock() < order.getQuantity()) {
            return R.error("库存不足");
        }
        order.setSellerId(goods.getSellerId());
        order.setAmount(goods.getPrice().multiply(new java.math.BigDecimal(order.getQuantity())));
        if (order.getPayType() == null) {
            order.setPayType(1);
        }
        order.setIsReserved(goods.getReserveSupport());
        if (!goodsService.reduceStock(goods.getId(), order.getQuantity())) {
            return R.error("库存扣减失败");
        }
        return R.ok(ordersService.createOrder(order));
    }

    @GetMapping("/{orderNo}")
    public R<OrderDetailDto> get(@PathVariable String orderNo) {
        Orders order = ordersService.getByOrderNo(orderNo);
        if (order == null) {
            return R.error("订单不存在");
        }
        OrderDetailDto dto = new OrderDetailDto();
        dto.setOrder(order);
        dto.setGoods(goodsService.getByIdIncludeAll(order.getGoodsId()));
        dto.setBuyer(userService.getById(order.getBuyerId()));
        dto.setSeller(userService.getById(order.getSellerId()));
        return R.ok(dto);
    }

    @GetMapping("/buyer/list")
    public R<List<OrderDetailDto>> listBuyer() {
        Long buyerId = LoginUserHolder.getUserId();
        List<Orders> orders = ordersService.listByBuyer(buyerId);
        List<OrderDetailDto> dtoList = orders.stream().map(order -> {
            OrderDetailDto dto = new OrderDetailDto();
            dto.setOrder(order);
            dto.setGoods(goodsService.getByIdIncludeAll(order.getGoodsId()));
            dto.setBuyer(userService.getById(order.getBuyerId()));
            dto.setSeller(userService.getById(order.getSellerId()));
            return dto;
        }).collect(Collectors.toList());
        return R.ok(dtoList);
    }

    @GetMapping("/seller/list")
    public R<List<OrderDetailDto>> listSeller() {
        Long sellerId = LoginUserHolder.getUserId();
        List<Orders> orders = ordersService.listBySeller(sellerId);
        List<OrderDetailDto> dtoList = orders.stream().map(order -> {
            OrderDetailDto dto = new OrderDetailDto();
            dto.setOrder(order);
            dto.setGoods(goodsService.getByIdIncludeAll(order.getGoodsId()));
            dto.setBuyer(userService.getById(order.getBuyerId()));
            dto.setSeller(userService.getById(order.getSellerId()));
            return dto;
        }).collect(Collectors.toList());
        return R.ok(dtoList);
    }

    @PostMapping("/ship")
    public R<String> ship(@RequestBody Orders request) {
        Orders order = ordersService.getByOrderNo(request.getOrderNo());
        if (order == null) {
            return R.error("订单不存在");
        }
        if (!order.getSellerId().equals(LoginUserHolder.getUserId())) {
            return R.error("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            return R.error("当前订单无法发货");
        }
        if (!ordersService.shipOrder(order.getOrderNo())) {
            return R.error("发货失败");
        }
        return R.ok("发货成功");
    }

    @PostMapping("/complete")
    public R<String> complete(@RequestBody Orders request) {
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
        if (!ordersService.completeOrder(order.getOrderNo())) {
            return R.error("确认收货失败");
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
        goodsService.increaseStock(order.getGoodsId(), order.getQuantity());
        return R.ok("订单已取消");
    }
}

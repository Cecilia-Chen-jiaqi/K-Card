package com.kpoptrade.controller;

import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrdersService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private GoodsService goodsService;

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
    public R<Orders> get(@PathVariable String orderNo) {
        return R.ok(ordersService.getByOrderNo(orderNo));
    }
}

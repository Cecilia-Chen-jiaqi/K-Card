package com.kpoptrade.controller;

import com.kpoptrade.entity.Cart;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.CartItemVo;
import com.kpoptrade.service.CartService;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrdersService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrdersService ordersService;

    @PostMapping("/add")
    public R<Cart> add(@RequestBody Cart cart) {
        cart.setUserId(LoginUserHolder.getUserId());
        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            cart.setQuantity(1);
        }
        return R.ok(cartService.addToCart(cart));
    }

    @GetMapping("/list")
    public R<List<CartItemVo>> list() {
        List<Cart> items = cartService.listByUser(LoginUserHolder.getUserId());
        List<CartItemVo> result = items.stream().map(cart -> {
            Goods goods = goodsService.getById(cart.getGoodsId());
            CartItemVo vo = new CartItemVo();
            vo.setCart(cart);
            vo.setGoods(goods);
            return vo;
        }).collect(Collectors.toList());
        return R.ok(result);
    }

    @PostMapping("/checkout/{cartId}")
    public R<Orders> checkout(@PathVariable Long cartId) {
        Cart cart = cartService.getById(cartId);
        if (cart == null || cart.getUserId() == null || !cart.getUserId().equals(LoginUserHolder.getUserId())) {
            return R.error("购物车记录不存在");
        }
        Goods goods = goodsService.getById(cart.getGoodsId());
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            return R.error("商品不存在或已下架");
        }
        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            cart.setQuantity(1);
        }
        if (goods.getStock() == null || goods.getStock() < cart.getQuantity()) {
            return R.error("库存不足");
        }
        Orders order = new Orders();
        order.setBuyerId(LoginUserHolder.getUserId());
        order.setSellerId(goods.getSellerId());
        order.setGoodsId(goods.getId());
        order.setQuantity(cart.getQuantity());
        order.setAmount(goods.getPrice().multiply(new java.math.BigDecimal(cart.getQuantity())));
        order.setPayType(1);
        order.setIsReserved(goods.getReserveSupport());
        Orders saved = ordersService.createOrder(order);
        cartService.removeById(cartId);
        goodsService.reduceStock(goods.getId(), cart.getQuantity());
        return R.ok(saved);
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody Cart cart) {
        return R.ok(cartService.updateCart(cart));
    }

    @DeleteMapping("/remove/{id}")
    public R<Boolean> remove(@PathVariable Long id) {
        return R.ok(cartService.removeById(id));
    }
}

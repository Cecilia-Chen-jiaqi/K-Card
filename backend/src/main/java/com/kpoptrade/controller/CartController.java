package com.kpoptrade.controller;

import com.kpoptrade.entity.Cart;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.CartItemVo;
import com.kpoptrade.service.CartService;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrdersService;
import com.kpoptrade.util.JwtUtil;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
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
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
public R<Cart> addCart(@RequestBody Cart cart, @RequestHeader("Authorization") String token) {
    // 1. 解析登录用户ID
    Long userId = jwtUtil.getUserId(token);
    cart.setUserId(userId);
    
    // 2. 使用 getByIdIncludeAll 替换 getById
    Goods goods = goodsService.getByIdIncludeAll(cart.getGoodsId());
    if(goods == null || goods.getStatus() != 1){
        return R.error("商品不存在或已下架，无法加入购物车");
    }
    
    // 3. 校验库存
    if(goods.getStock() < cart.getQuantity()){
        return R.error("商品库存不足");
    }
    
    // 4. 存在则更新数量，不存在新增记录
    cartService.addToCart(cart);
    return R.ok("加入购物车成功");
}


    @GetMapping("/list")
    public R<List<CartItemVo>> list() {
        List<Cart> items = cartService.listByUser(LoginUserHolder.getUserId());
        // 🚨修复：过滤掉商品已被彻底删除的脏数据，防止前端空指针
        List<CartItemVo> result = items.stream().map(cart -> {
            Goods goods = goodsService.getById(cart.getGoodsId());
            CartItemVo vo = new CartItemVo();
            vo.setCart(cart);
            vo.setGoods(goods);
            return vo;
        }).filter(vo -> Objects.nonNull(vo.getGoods())).collect(Collectors.toList());
        return R.ok(result);
    }

    @PostMapping("/checkout/{cartId}")
    public R<Orders> checkout(@PathVariable Long cartId) {
        Cart cart = cartService.getById(cartId);
        if (cart == null || cart.getUserId() == null || !cart.getUserId().equals(LoginUserHolder.getUserId())) {
            return R.error("购物车记录不存在");
        }
        // CartController.java 加购逻辑核心验证点
        Goods goods = goodsService.getByIdIncludeAll(cart.getGoodsId()); // 必须是此方法
        if (goods == null) { return R.error("商品不存在"); }
        if (goods.getStatus() != 1) { return R.error("商品已下架"); }   // 手动校验状态
        if (goods.getStock() < cart.getQuantity()) { return R.error("库存不足"); }

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
        
        // 🚨修复：必须先扣库存，再删购物车！防止扣库存异常导致购物车记录丢失
        goodsService.reduceStock(goods.getId(), cart.getQuantity());
        cartService.removeById(cartId);
        
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

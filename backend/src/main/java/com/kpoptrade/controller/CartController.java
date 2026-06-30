package com.kpoptrade.controller;



import com.kpoptrade.dto.CartCheckoutBatchDto;

import com.kpoptrade.entity.Address;

import com.kpoptrade.entity.Cart;

import com.kpoptrade.entity.CartItemVo;

import com.kpoptrade.entity.Goods;

import com.kpoptrade.entity.OrderItem;

import com.kpoptrade.entity.Orders;

import com.kpoptrade.service.AddressService;

import com.kpoptrade.service.CartService;

import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.OrdersService;

import com.kpoptrade.util.LoginUserHolder;

import com.kpoptrade.util.R;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.*;



import java.math.BigDecimal;

import java.util.ArrayList;

import java.util.HashSet;

import java.util.List;

import java.util.Objects;

import java.util.Set;

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

    private AddressService addressService;

    @PostMapping("/add")
    public R<Cart> addCart(@RequestBody Cart cart) {

        Long userId = LoginUserHolder.getUserId();

        if (userId == null) {

            return R.error("请先登录");

        }

        cart.setUserId(userId);

        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {

            cart.setQuantity(1);

        }

        if (cart.getSelected() == null) {

            cart.setSelected(1);

        }



        Goods goods = goodsService.getByIdIncludeAll(cart.getGoodsId());

        if (goods == null || goods.getStatus() != 1) {

            return R.error("商品不存在或已下架，无法加入购物车");

        }

        try {

            goodsService.ensureNotSelfPurchase(userId, goods.getSellerId());

        } catch (IllegalArgumentException e) {

            return R.error(e.getMessage());

        }

        if (goods.getStock() == null || goods.getStock() < cart.getQuantity()) {

            return R.error("商品库存不足");

        }



        cartService.addToCart(cart);

        return R.ok("加入购物车成功", cart);

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

        }).filter(vo -> Objects.nonNull(vo.getGoods())).collect(Collectors.toList());

        return R.ok(result);

    }



    @PostMapping("/checkout/{cartId}")

    @Transactional

    public R<Orders> checkout(@PathVariable Long cartId) {

        CartCheckoutBatchDto dto = new CartCheckoutBatchDto();

        dto.setCartIds(java.util.Collections.singletonList(cartId));

        return checkoutBatch(dto);

    }



    @PostMapping("/checkout-batch")

    @Transactional

    public R<Orders> checkoutBatch(@RequestBody CartCheckoutBatchDto dto) {

        Long buyerId = LoginUserHolder.getUserId();

        if (buyerId == null) {

            return R.error("请先登录");

        }

        List<Long> cartIds = dto != null ? dto.getCartIds() : null;

        if (cartIds == null || cartIds.isEmpty()) {

            return R.error("请选择要结算的商品");

        }



        List<Cart> carts = new ArrayList<>();

        for (Long cartId : cartIds) {

            Cart cart = cartService.getById(cartId);

            if (cart == null || cart.getUserId() == null || !cart.getUserId().equals(buyerId)) {

                return R.error("购物车记录不存在");

            }

            carts.add(cart);

        }



        List<OrderItem> lines = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        int totalQty = 0;

        Set<Long> sellerIds = new HashSet<>();

        boolean needsMail = false;

        int reservedFlag = 0;



        for (Cart cart : carts) {

            Goods goods = goodsService.getByIdIncludeAll(cart.getGoodsId());

            if (goods == null) {

                return R.error("商品不存在");

            }

            if (goods.getStatus() != 1) {

                return R.error("商品「" + goods.getTitle() + "」已下架");

            }

            try {

                goodsService.ensureNotSelfPurchase(buyerId, goods.getSellerId());

            } catch (IllegalArgumentException e) {

                return R.error(e.getMessage());

            }

            int qty = cart.getQuantity() == null || cart.getQuantity() <= 0 ? 1 : cart.getQuantity();

            if (goods.getStock() == null || goods.getStock() < qty) {

                return R.error("商品「" + goods.getTitle() + "」库存不足");

            }



            OrderItem line = new OrderItem();

            line.setGoodsId(goods.getId());

            line.setSellerId(goods.getSellerId());

            line.setQuantity(qty);

            line.setUnitPrice(goods.getPrice());

            line.setAmount(goods.getPrice().multiply(BigDecimal.valueOf(qty)));

            lines.add(line);



            totalAmount = totalAmount.add(line.getAmount());

            totalQty += qty;

            sellerIds.add(goods.getSellerId());

            if (goods.getDeliveryMode() == null || goods.getDeliveryMode() != 2) {

                needsMail = true;

            }

            if (goods.getReserveSupport() != null && goods.getReserveSupport() == 1) {

                reservedFlag = 1;

            }

        }



        Orders order = new Orders();

        order.setBuyerId(buyerId);

        order.setPayType(1);

        order.setIsReserved(reservedFlag);

        order.setAmount(totalAmount);

        order.setQuantity(totalQty);

        order.setItemCount(lines.size());



        if (lines.size() == 1) {

            OrderItem only = lines.get(0);

            order.setGoodsId(only.getGoodsId());

            order.setSellerId(only.getSellerId());

        } else {

            order.setGoodsId(null);

            order.setSellerId(sellerIds.size() == 1 ? sellerIds.iterator().next() : null);

        }



        if (needsMail) {

            Address defaultAddr = addressService.getDefaultByUser(buyerId);

            if (defaultAddr != null) {

                order.setAddressId(defaultAddr.getId());

            }

        }



        Orders saved = ordersService.createOrderWithItems(order, lines);

        for (Long cartId : cartIds) {

            cartService.removeById(cartId);

        }

        return R.ok(saved);

    }



    @PostMapping("/update")

    public R<Boolean> update(@RequestBody Cart cart) {

        Long userId = LoginUserHolder.getUserId();

        if (cart.getId() == null) {

            return R.error("参数错误");

        }

        Cart existing = cartService.getById(cart.getId());

        if (existing == null || existing.getUserId() == null || !existing.getUserId().equals(userId)) {

            return R.error("购物车记录不存在");

        }

        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {

            return R.error("数量无效");

        }

        Goods goods = goodsService.getByIdIncludeAll(existing.getGoodsId());

        if (goods == null || goods.getStatus() != 1) {

            return R.error("商品已下架");

        }

        if (goods.getStock() == null || goods.getStock() < cart.getQuantity()) {

            return R.error("库存不足");

        }

        existing.setQuantity(cart.getQuantity());

        return R.ok(cartService.updateCart(existing));

    }



    @DeleteMapping("/remove/{id}")

    public R<Boolean> remove(@PathVariable Long id) {

        Long userId = LoginUserHolder.getUserId();

        Cart existing = cartService.getById(id);

        if (existing == null || existing.getUserId() == null || !existing.getUserId().equals(userId)) {

            return R.error("购物车记录不存在");

        }

        return R.ok(cartService.removeById(id));

    }

}



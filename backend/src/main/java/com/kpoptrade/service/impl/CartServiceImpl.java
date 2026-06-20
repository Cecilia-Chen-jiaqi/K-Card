package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.Cart;
import com.kpoptrade.mapper.CartMapper;
import com.kpoptrade.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
    @Override
    public Cart addToCart(Cart cart) {
        Cart existing = lambdaQuery().eq(Cart::getUserId, cart.getUserId()).eq(Cart::getGoodsId, cart.getGoodsId()).one();
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + cart.getQuantity());
            updateById(existing);
            return existing;
        }
        save(cart);
        return cart;
    }
    @Override
    public boolean saveOrUpdate(Cart cart) {
        return super.saveOrUpdate(cart);
    }

    @Override
    public Cart getById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<Cart> listByUser(Long userId) {
        return lambdaQuery().eq(Cart::getUserId, userId).list();
    }

    @Override
    public boolean updateCart(Cart cart) {
        return updateById(cart);
    }

    @Override
    public boolean removeById(Long id) {
        return super.removeById(id);
    }
}

package com.kpoptrade.service;

import com.kpoptrade.entity.Cart;

import java.util.List;

// CartService.java
public interface CartService {
    Cart addToCart(Cart cart);
    Cart getById(Long id);
    List<Cart> listByUser(Long userId);
    boolean updateCart(Cart cart);
    boolean removeById(Long id);
    
    // 新增声明
    boolean saveOrUpdate(Cart cart); 
}

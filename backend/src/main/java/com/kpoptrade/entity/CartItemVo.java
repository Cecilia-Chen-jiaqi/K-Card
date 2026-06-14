package com.kpoptrade.entity;

import lombok.Data;

@Data
public class CartItemVo {
    private Cart cart;
    private Goods goods;
}

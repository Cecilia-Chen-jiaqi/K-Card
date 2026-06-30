package com.kpoptrade.dto;

import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.OrderItem;
import com.kpoptrade.entity.User;
import lombok.Data;

@Data
public class OrderLineDto {
    private OrderItem item;
    private Goods goods;
    private User seller;
}

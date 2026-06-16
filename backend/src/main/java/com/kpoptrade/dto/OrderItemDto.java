package com.kpoptrade.dto;

import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.User;
import lombok.Data;

@Data
public class OrderItemDto {
    private Orders order;
    private Goods goods;
    private User buyer;
    private User seller;
}

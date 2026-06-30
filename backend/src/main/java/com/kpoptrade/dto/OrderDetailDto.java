package com.kpoptrade.dto;

import com.kpoptrade.entity.Address;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailDto {
    private Orders order;
    private Goods goods;
    private User buyer;
    private User seller;
    private Address address;
    /** 合并订单明细（含多商品） */
    private List<OrderLineDto> items;
    /** 当前卖家视角下的明细（卖家订单列表/发货用） */
    private List<OrderLineDto> sellerItems;
    private Boolean multiItem;
}

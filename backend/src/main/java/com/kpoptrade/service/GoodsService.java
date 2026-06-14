package com.kpoptrade.service;

import com.kpoptrade.entity.Goods;

import java.util.List;

public interface GoodsService {
    Goods createGoods(Goods goods);
    Goods getById(Long id);
    boolean updateGoods(Goods goods);
    boolean reduceStock(Long goodsId, Integer quantity);
    List<Goods> listAll();
    List<Goods> listCampusGoods();
    List<Goods> listExchangeGoods();
}

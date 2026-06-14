package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.mapper.GoodsMapper;
import com.kpoptrade.service.GoodsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Override
    public Goods createGoods(Goods goods) {
        goods.setStatus(1);
        save(goods);
        return goods;
    }

    @Override
    public Goods getById(Long id) {
        return lambdaQuery().eq(Goods::getId, id).eq(Goods::getStatus, 1).one();
    }

    @Override
    public boolean updateGoods(Goods goods) {
        return updateById(goods);
    }

    @Override
    public boolean reduceStock(Long goodsId, Integer quantity) {
        if (goodsId == null || quantity == null || quantity <= 0) {
            return false;
        }
        Goods goods = getById(goodsId);
        if (goods == null || goods.getStock() == null || goods.getStock() < quantity) {
            return false;
        }
        goods.setStock(goods.getStock() - quantity);
        return updateById(goods);
    }

    @Override
    public List<Goods> listAll() {
        return lambdaQuery().eq(Goods::getStatus, 1).list();
    }

    @Override
    public List<Goods> listCampusGoods() {
        return lambdaQuery().eq(Goods::getDeliveryMode, 2).eq(Goods::getStatus, 1).list();
    }

    @Override
    public List<Goods> listExchangeGoods() {
        return lambdaQuery().ne(Goods::getTradeType, "仅出售").eq(Goods::getStatus, 1).list();
    }
}

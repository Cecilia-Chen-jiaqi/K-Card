package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.constant.GoodsStatus;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.mapper.GoodsMapper;
import com.kpoptrade.service.GoodsService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    @Override
    public Goods createGoods(Goods goods) {
        Date now = new Date();
        goods.setStatus(GoodsStatus.PENDING);
        goods.setCreatedAt(now);
        goods.setUpdatedAt(now);
        save(goods);
        return goods;
    }

    @Override
    public Goods getById(Long id) {
        return lambdaQuery().eq(Goods::getId, id).eq(Goods::getStatus, 1).one();
    }

    @Override
    public Goods getByIdIncludeAll(Long id) {
        return lambdaQuery().eq(Goods::getId, id).one();
    }

    @Override
    public boolean updateGoods(Goods goods) {
        return updateById(goods);
    }

    @Override
    public void ensureNotSelfPurchase(Long buyerId, Long sellerId) {
        if (buyerId != null && sellerId != null && buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("不能购买自己发布的小卡");
        }
    }

    @Override
    public void delistIfSoldOut(Long goodsId) {
        if (goodsId == null) {
            return;
        }
        lambdaUpdate()
                .eq(Goods::getId, goodsId)
                .le(Goods::getStock, 0)
                .set(Goods::getStatus, 0)
                .update();
    }

    @Override
    public void fulfillPaidOrderStock(Long goodsId, Integer quantity) {
        if (goodsId == null || quantity == null || quantity <= 0) {
            return;
        }
        Goods goods = getByIdIncludeAll(goodsId);
        if (goods == null) {
            return;
        }
        if (goods.getStatus() != null && goods.getStatus() == 1
                && goods.getStock() != null && goods.getStock() >= quantity) {
            reduceStock(goodsId, quantity);
            return;
        }
        if (goods.getStock() != null && goods.getStock() <= 0
                && (goods.getStatus() == null || goods.getStatus() == 1)) {
            delistIfSoldOut(goodsId);
        }
    }

    @Override
    public boolean reduceStock(Long goodsId, Integer quantity) {
        if (goodsId == null || quantity == null || quantity <= 0) {
            return false;
        }
        Goods goods = getByIdIncludeAll(goodsId);
        if (goods == null || goods.getStatus() == null || goods.getStatus() != 1) {
            return false;
        }
        if (goods.getStock() == null || goods.getStock() < quantity) {
            return false;
        }
        int remaining = goods.getStock() - quantity;
        goods.setStock(Math.max(remaining, 0));
        if (goods.getStock() <= 0) {
            goods.setStatus(0);
        }
        boolean updated = updateById(goods);
        if (updated && goods.getStock() <= 0) {
            delistIfSoldOut(goodsId);
        }
        return updated;
    }

    @Override
    public boolean increaseStock(Long goodsId, Integer quantity) {
        if (goodsId == null || quantity == null || quantity <= 0) {
            return false;
        }
        Goods goods = lambdaQuery().eq(Goods::getId, goodsId).one();
        if (goods == null) {
            return false;
        }
        Integer currentStock = goods.getStock() == null ? 0 : goods.getStock();
        goods.setStock(currentStock + quantity);
        if (goods.getStock() > 0 && goods.getStatus() != null && goods.getStatus() == GoodsStatus.OFF) {
            goods.setStatus(GoodsStatus.ON);
        }
        return updateById(goods);
    }

    @Override
    public List<Goods> listAll() {
        return lambdaQuery().eq(Goods::getStatus, GoodsStatus.ON).gt(Goods::getStock, 0).list();
    }

    @Override
    public List<Goods> listCampusGoods() {
        return lambdaQuery().eq(Goods::getDeliveryMode, 2).eq(Goods::getStatus, GoodsStatus.ON).gt(Goods::getStock, 0).list();
    }

    @Override
    public List<Goods> listExchangeGoods() {
        return lambdaQuery().ne(Goods::getTradeType, "仅出售").eq(Goods::getStatus, GoodsStatus.ON).gt(Goods::getStock, 0).list();
    }

    @Override
    public List<Goods> search(String keyword, String groupName, String idolName, String cardType,
                              String tradeType, String quality, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice) {
        return lambdaQuery()
                .eq(Goods::getStatus, GoodsStatus.ON)
                .gt(Goods::getStock, 0)
                .like(keyword != null && !keyword.trim().isEmpty(), Goods::getTitle, keyword)
                .eq(groupName != null && !groupName.trim().isEmpty(), Goods::getGroupName, groupName)
                .eq(idolName != null && !idolName.trim().isEmpty(), Goods::getIdolName, idolName)
                .eq(cardType != null && !cardType.trim().isEmpty(), Goods::getCardType, cardType)
                .eq(tradeType != null && !tradeType.trim().isEmpty(), Goods::getTradeType, tradeType)
                .eq(quality != null && !quality.trim().isEmpty(), Goods::getQuality, quality)
                .ge(minPrice != null, Goods::getPrice, minPrice)
                .le(maxPrice != null, Goods::getPrice, maxPrice)
                .orderByDesc(Goods::getId)
                .list();
    }

    @Override
    public List<Goods> listBySeller(Long sellerId) {
        return lambdaQuery().eq(Goods::getSellerId, sellerId).orderByDesc(Goods::getId).list();
    }
}

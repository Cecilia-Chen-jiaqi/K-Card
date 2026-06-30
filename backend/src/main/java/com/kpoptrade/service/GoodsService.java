package com.kpoptrade.service;

import com.kpoptrade.entity.Goods;

import java.util.List;

public interface GoodsService {
    Goods createGoods(Goods goods);
    Goods getById(Long id);
    Goods getByIdIncludeAll(Long id);
    boolean updateGoods(Goods goods);
    boolean reduceStock(Long goodsId, Integer quantity);

    boolean increaseStock(Long goodsId, Integer quantity);

    /** 校验买家不能购买自己发布的商品 */
    void ensureNotSelfPurchase(Long buyerId, Long sellerId);

    /** 库存售罄时自动下架（status=0） */
    void delistIfSoldOut(Long goodsId);

    /** 已支付订单补扣库存并下架（幂等，用于同步/修复） */
    void fulfillPaidOrderStock(Long goodsId, Integer quantity);
    List<Goods> listAll();
    List<Goods> listCampusGoods();
    List<Goods> listExchangeGoods();
    List<Goods> search(String keyword, String groupName, String idolName, String cardType,
                       String tradeType, String quality, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice);
    List<Goods> listBySeller(Long sellerId);
}

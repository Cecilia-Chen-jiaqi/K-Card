package com.kpoptrade.service;

import com.kpoptrade.entity.GoodsFavorite;

import java.util.List;

public interface GoodsFavoriteService {
    GoodsFavorite addFavorite(Long userId, Long goodsId);

    boolean removeFavorite(Long userId, Long goodsId);

    List<GoodsFavorite> listByUser(Long userId);

    boolean isFavorited(Long userId, Long goodsId);
}

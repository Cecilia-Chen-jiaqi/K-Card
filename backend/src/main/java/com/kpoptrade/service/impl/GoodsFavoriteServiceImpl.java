package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.GoodsFavorite;
import com.kpoptrade.mapper.GoodsFavoriteMapper;
import com.kpoptrade.service.GoodsFavoriteService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class GoodsFavoriteServiceImpl extends ServiceImpl<GoodsFavoriteMapper, GoodsFavorite> implements GoodsFavoriteService {
    @Override
    public GoodsFavorite addFavorite(Long userId, Long goodsId) {
        GoodsFavorite existing = lambdaQuery()
                .eq(GoodsFavorite::getUserId, userId)
                .eq(GoodsFavorite::getGoodsId, goodsId)
                .one();
        if (existing != null) {
            return existing;
        }
        GoodsFavorite favorite = new GoodsFavorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setCreatedAt(new Date());
        save(favorite);
        return favorite;
    }

    @Override
    public boolean removeFavorite(Long userId, Long goodsId) {
        return lambdaUpdate()
                .eq(GoodsFavorite::getUserId, userId)
                .eq(GoodsFavorite::getGoodsId, goodsId)
                .remove();
    }

    @Override
    public List<GoodsFavorite> listByUser(Long userId) {
        return lambdaQuery()
                .eq(GoodsFavorite::getUserId, userId)
                .orderByDesc(GoodsFavorite::getCreatedAt)
                .list();
    }

    @Override
    public boolean isFavorited(Long userId, Long goodsId) {
        if (userId == null || goodsId == null) {
            return false;
        }
        return lambdaQuery()
                .eq(GoodsFavorite::getUserId, userId)
                .eq(GoodsFavorite::getGoodsId, goodsId)
                .count() > 0;
    }
}

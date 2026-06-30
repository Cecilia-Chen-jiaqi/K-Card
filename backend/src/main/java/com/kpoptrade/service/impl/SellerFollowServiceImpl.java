package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.SellerFollow;
import com.kpoptrade.mapper.SellerFollowMapper;
import com.kpoptrade.service.SellerFollowService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SellerFollowServiceImpl extends ServiceImpl<SellerFollowMapper, SellerFollow> implements SellerFollowService {
    @Override
    public SellerFollow addFollow(Long userId, Long sellerId) {
        SellerFollow existing = lambdaQuery()
                .eq(SellerFollow::getUserId, userId)
                .eq(SellerFollow::getSellerId, sellerId)
                .one();
        if (existing != null) {
            return existing;
        }
        SellerFollow follow = new SellerFollow();
        follow.setUserId(userId);
        follow.setSellerId(sellerId);
        follow.setCreatedAt(new Date());
        save(follow);
        return follow;
    }

    @Override
    public boolean removeFollow(Long userId, Long sellerId) {
        return lambdaUpdate()
                .eq(SellerFollow::getUserId, userId)
                .eq(SellerFollow::getSellerId, sellerId)
                .remove();
    }

    @Override
    public List<SellerFollow> listByUser(Long userId) {
        return lambdaQuery()
                .eq(SellerFollow::getUserId, userId)
                .orderByDesc(SellerFollow::getCreatedAt)
                .list();
    }

    @Override
    public boolean isFollowing(Long userId, Long sellerId) {
        if (userId == null || sellerId == null) {
            return false;
        }
        return lambdaQuery()
                .eq(SellerFollow::getUserId, userId)
                .eq(SellerFollow::getSellerId, sellerId)
                .count() > 0;
    }
}

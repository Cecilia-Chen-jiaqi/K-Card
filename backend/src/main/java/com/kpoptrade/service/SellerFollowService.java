package com.kpoptrade.service;

import com.kpoptrade.entity.SellerFollow;

import java.util.List;

public interface SellerFollowService {
    SellerFollow addFollow(Long userId, Long sellerId);

    boolean removeFollow(Long userId, Long sellerId);

    List<SellerFollow> listByUser(Long userId);

    boolean isFollowing(Long userId, Long sellerId);
}

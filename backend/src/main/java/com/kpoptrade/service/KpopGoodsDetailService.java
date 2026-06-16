package com.kpoptrade.service;

import com.kpoptrade.entity.KpopGoodsDetail;

public interface KpopGoodsDetailService {
    KpopGoodsDetail findByGoodsId(Long goodsId);
    boolean saveOrUpdate(KpopGoodsDetail detail);
}

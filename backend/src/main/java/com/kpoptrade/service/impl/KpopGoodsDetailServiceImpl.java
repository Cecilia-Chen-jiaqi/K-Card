package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.KpopGoodsDetail;
import com.kpoptrade.mapper.KpopGoodsDetailMapper;
import com.kpoptrade.service.KpopGoodsDetailService;
import org.springframework.stereotype.Service;

@Service
public class KpopGoodsDetailServiceImpl extends ServiceImpl<KpopGoodsDetailMapper, KpopGoodsDetail> implements KpopGoodsDetailService {
    @Override
    public KpopGoodsDetail findByGoodsId(Long goodsId) {
        return lambdaQuery().eq(KpopGoodsDetail::getGoodsId, goodsId).one();
    }

   @Override
public boolean saveOrUpdate(KpopGoodsDetail detail) {
    if (detail == null || detail.getGoodsId() == null) {
        return false;
    }
    KpopGoodsDetail existing = findByGoodsId(detail.getGoodsId());
    if (existing != null) {
        detail.setId(existing.getId());
    }
    return super.saveOrUpdate(detail);
}
}

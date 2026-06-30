package com.kpoptrade.controller;

import com.kpoptrade.entity.FavoriteItemVo;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.GoodsFavorite;
import com.kpoptrade.service.GoodsFavoriteService;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    @Autowired
    private GoodsFavoriteService goodsFavoriteService;
    @Autowired
    private GoodsService goodsService;

    @PostMapping("/add")
    public R<GoodsFavorite> add(@RequestBody Map<String, Object> body) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        Object goodsIdObj = body.get("goodsId");
        if (goodsIdObj == null) {
            return R.error("请选择商品");
        }
        Long goodsId = Long.valueOf(goodsIdObj.toString());
        Goods goods = goodsService.getByIdIncludeAll(goodsId);
        if (goods == null) {
            return R.error("商品不存在");
        }
        if (goods.getStatus() == null || goods.getStatus() != 1) {
            return R.error("商品已下架，无法收藏");
        }
        try {
            goodsService.ensureNotSelfPurchase(userId, goods.getSellerId());
        } catch (IllegalArgumentException e) {
            return R.error("不能收藏自己发布的小卡");
        }
        return R.ok("收藏成功", goodsFavoriteService.addFavorite(userId, goodsId));
    }

    @DeleteMapping("/remove/{goodsId}")
    public R<Boolean> remove(@PathVariable Long goodsId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        if (!goodsFavoriteService.removeFavorite(userId, goodsId)) {
            return R.error("收藏记录不存在");
        }
        return R.ok(true);
    }

    @GetMapping("/list")
    public R<List<FavoriteItemVo>> list() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        List<FavoriteItemVo> result = goodsFavoriteService.listByUser(userId).stream().map(fav -> {
            FavoriteItemVo vo = new FavoriteItemVo();
            vo.setFavorite(fav);
            vo.setGoods(goodsService.getByIdIncludeAll(fav.getGoodsId()));
            return vo;
        }).filter(vo -> Objects.nonNull(vo.getGoods())).collect(Collectors.toList());
        return R.ok(result);
    }

    @GetMapping("/check/{goodsId}")
    public R<Map<String, Boolean>> check(@PathVariable Long goodsId) {
        Long userId = LoginUserHolder.getUserId();
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", userId != null && goodsFavoriteService.isFavorited(userId, goodsId));
        return R.ok(result);
    }
}

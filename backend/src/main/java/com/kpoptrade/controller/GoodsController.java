package com.kpoptrade.controller;

import com.kpoptrade.entity.Goods;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("/list")
    public R<List<Goods>> listAll() {
        return R.ok(goodsService.listAll());
    }

    @GetMapping("/campus")
    public R<List<Goods>> listCampus() {
        return R.ok(goodsService.listCampusGoods());
    }

    @GetMapping("/exchange")
    public R<List<Goods>> listExchange() {
        return R.ok(goodsService.listExchangeGoods());
    }

    @GetMapping("/{id}")
    public R<Goods> getById(@PathVariable Long id) {
        return R.ok(goodsService.getById(id));
    }

    @PostMapping("/create")
    public R<Goods> create(@RequestBody Goods goods) {
        goods.setSellerId(LoginUserHolder.getUserId());
        return R.ok(goodsService.createGoods(goods));
    }
}

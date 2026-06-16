package com.kpoptrade.controller;

import com.kpoptrade.dto.GoodsCreateDto;
import com.kpoptrade.dto.GoodsDetailDto;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.KpopGoodsDetail;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.KpopGoodsDetailService;
import com.kpoptrade.service.UserService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private KpopGoodsDetailService kpopGoodsDetailService;
    @Autowired
    private UserService userService;

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

    @GetMapping("/detail")
    public R<GoodsDetailDto> getGoodsDetail(@RequestParam Long id) {
        Goods goods = goodsService.getByIdIncludeAll(id);
        if (goods == null) {
            return R.error("商品不存在");
        }
        if (goods.getStatus() == null || goods.getStatus() == 0) {
            return R.error("商品已下架");
        }
        GoodsDetailDto detailDto = new GoodsDetailDto();
        BeanUtils.copyProperties(goods, detailDto);
        KpopGoodsDetail detail = kpopGoodsDetailService.findByGoodsId(id);
        if (detail != null) {
            detailDto.setCardBundle(detail.getCardBundle());
            detailDto.setExchangeInfo(detail.getExchangeInfo());
            detailDto.setReserveDeadline(detail.getReserveDeadline());
            detailDto.setExtraInfo(detail.getExtraInfo());
        }
        User seller = userService.getById(goods.getSellerId());
        if (seller != null) {
            detailDto.setSellerUsername(seller.getUsername());
            detailDto.setSellerCampus(seller.getCampus());
        }
        return R.ok(detailDto);
    }

    @GetMapping("/{id}")
    public R<Goods> getById(@PathVariable Long id) {
        return R.ok(goodsService.getById(id));
    }

    @PostMapping("/create")
    public R<Goods> create(@RequestBody GoodsCreateDto createDto) {
        if (createDto == null
                || createDto.getTitle() == null || createDto.getTitle().trim().isEmpty()
                || createDto.getPrice() == null
                || createDto.getStock() == null
                || createDto.getGroupName() == null || createDto.getGroupName().trim().isEmpty()
                || createDto.getIdolName() == null || createDto.getIdolName().trim().isEmpty()) {
            return R.error("请填写完整的商品信息");
        }

        Goods goods = new Goods();
        BeanUtils.copyProperties(createDto, goods);
        goods.setSellerId(LoginUserHolder.getUserId());
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        Goods saved = goodsService.createGoods(goods);

        KpopGoodsDetail detail = new KpopGoodsDetail();
        detail.setGoodsId(saved.getId());
        detail.setCardBundle(createDto.getCardBundle());
        detail.setExchangeInfo(createDto.getExchangeInfo());
        detail.setExtraInfo(createDto.getExtraInfo());
        if (createDto.getReserveDeadline() != null && !createDto.getReserveDeadline().trim().isEmpty()) {
            try {
                String time = createDto.getReserveDeadline().replace('T', ' ');
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                detail.setReserveDeadline(date);
            } catch (Exception ignored) {
            }
        }
        kpopGoodsDetailService.saveOrUpdate(detail);
        return R.ok(saved);
    }
}

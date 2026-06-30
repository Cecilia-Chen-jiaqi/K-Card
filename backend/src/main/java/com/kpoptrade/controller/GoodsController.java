package com.kpoptrade.controller;

import com.kpoptrade.constant.GoodsStatus;
import com.kpoptrade.dto.GoodsCreateDto;
import com.kpoptrade.dto.GoodsDetailDto;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.KpopGoodsDetail;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.GoodsFavoriteService;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.KpopGoodsDetailService;
import com.kpoptrade.service.SellerFollowService;
import com.kpoptrade.service.UserService;
import com.kpoptrade.util.LoginUserHolder;
import com.kpoptrade.util.R;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    @Autowired
    private GoodsFavoriteService goodsFavoriteService;
    @Autowired
    private SellerFollowService sellerFollowService;

    @GetMapping("/list")
    public R<List<Goods>> listAll() {
        return R.ok(goodsService.listAll());
    }

    @GetMapping("/search")
    public R<List<Goods>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String idolName,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) String tradeType,
            @RequestParam(required = false) String quality,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return R.ok(goodsService.search(keyword, groupName, idolName, cardType, tradeType, quality, minPrice, maxPrice));
    }

    @GetMapping("/my")
    public R<List<Goods>> myListings() {
        return R.ok(goodsService.listBySeller(LoginUserHolder.getUserId()));
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
        if (goods.getStatus() == null || goods.getStatus() != GoodsStatus.ON) {
            Long viewerId = LoginUserHolder.getUserId();
            User viewer = viewerId != null ? userService.getById(viewerId) : null;
            boolean owner = viewerId != null && viewerId.equals(goods.getSellerId());
            boolean admin = viewer != null && viewer.getRole() != null && viewer.getRole() == 1;
            if (!owner && !admin) {
                return R.error("商品已下架");
            }
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
        Long viewerId = LoginUserHolder.getUserId();
        if (viewerId != null) {
            detailDto.setFavorited(goodsFavoriteService.isFavorited(viewerId, id));
            detailDto.setFollowingSeller(sellerFollowService.isFollowing(viewerId, goods.getSellerId()));
        } else {
            detailDto.setFavorited(false);
            detailDto.setFollowingSeller(false);
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

    @PostMapping("/delist/{id}")
    public R<String> delist(@PathVariable Long id) {
        Goods goods = goodsService.getByIdIncludeAll(id);
        if (goods == null) {
            return R.error("商品不存在");
        }
        if (!goods.getSellerId().equals(LoginUserHolder.getUserId())) {
            return R.error("无权操作此商品");
        }
        if (goods.getStatus() != null && goods.getStatus() == 0) {
            return R.ok("商品已下架");
        }
        goods.setStatus(0);
        if (!goodsService.updateGoods(goods)) {
            return R.error("下架失败");
        }
        return R.ok("已下架");
    }

    @PostMapping("/relist/{id}")
    public R<String> relist(@PathVariable Long id) {
        Goods goods = goodsService.getByIdIncludeAll(id);
        if (goods == null) {
            return R.error("商品不存在");
        }
        if (!goods.getSellerId().equals(LoginUserHolder.getUserId())) {
            return R.error("无权操作此商品");
        }
        if (goods.getStock() == null || goods.getStock() <= 0) {
            return R.error("库存为 0，无法重新上架");
        }
        goods.setStatus(GoodsStatus.PENDING);
        goods.setRejectReason(null);
        goods.setReviewedAt(null);
        goods.setReviewedBy(null);
        if (!goodsService.updateGoods(goods)) {
            return R.error("上架失败");
        }
        return R.ok("已提交审核，请等待管理员处理");
    }
}

package com.kpoptrade.controller;

import com.kpoptrade.entity.FollowItemVo;
import com.kpoptrade.entity.SellerFollow;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.SellerFollowService;
import com.kpoptrade.service.UserService;
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
@RequestMapping("/api/follow")
public class FollowController {
    @Autowired
    private SellerFollowService sellerFollowService;
    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public R<SellerFollow> add(@RequestBody Map<String, Object> body) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        Object sellerIdObj = body.get("sellerId");
        if (sellerIdObj == null) {
            return R.error("请选择卖家");
        }
        Long sellerId = Long.valueOf(sellerIdObj.toString());
        if (userId.equals(sellerId)) {
            return R.error("不能关注自己");
        }
        User seller = userService.getById(sellerId);
        if (seller == null) {
            return R.error("卖家不存在");
        }
        return R.ok("关注成功", sellerFollowService.addFollow(userId, sellerId));
    }

    @DeleteMapping("/remove/{sellerId}")
    public R<Boolean> remove(@PathVariable Long sellerId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        if (!sellerFollowService.removeFollow(userId, sellerId)) {
            return R.error("关注记录不存在");
        }
        return R.ok(true);
    }

    @GetMapping("/list")
    public R<List<FollowItemVo>> list() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            return R.error("请先登录");
        }
        List<FollowItemVo> result = sellerFollowService.listByUser(userId).stream().map(follow -> {
            FollowItemVo vo = new FollowItemVo();
            vo.setFollow(follow);
            vo.setSeller(userService.getById(follow.getSellerId()));
            return vo;
        }).filter(vo -> Objects.nonNull(vo.getSeller())).collect(Collectors.toList());
        return R.ok(result);
    }

    @GetMapping("/check/{sellerId}")
    public R<Map<String, Boolean>> check(@PathVariable Long sellerId) {
        Long userId = LoginUserHolder.getUserId();
        Map<String, Boolean> result = new HashMap<>();
        result.put("following", userId != null && sellerFollowService.isFollowing(userId, sellerId));
        return R.ok(result);
    }
}

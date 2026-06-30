package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kpoptrade.constant.AccountStatus;
import com.kpoptrade.constant.GoodsStatus;
import com.kpoptrade.constant.UserRole;
import com.kpoptrade.dto.AdminGoodsDto;
import com.kpoptrade.dto.AdminStatsDto;
import com.kpoptrade.dto.AdminUserDto;
import com.kpoptrade.dto.DailyTrendPoint;
import com.kpoptrade.dto.PageResult;
import com.kpoptrade.entity.Goods;
import com.kpoptrade.entity.Orders;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.AdminService;
import com.kpoptrade.service.GoodsService;
import com.kpoptrade.service.impl.GoodsServiceImpl;
import com.kpoptrade.service.impl.OrdersServiceImpl;
import com.kpoptrade.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsServiceImpl goodsServiceImpl;
    @Autowired
    private OrdersServiceImpl ordersServiceImpl;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public AdminStatsDto getOverviewStats() {
        AdminStatsDto dto = new AdminStatsDto();
        dto.setUserCount(count("SELECT COUNT(*) FROM user"));
        dto.setGoodsPending(countGoodsByStatus(GoodsStatus.PENDING));
        dto.setGoodsOnSale(countGoodsByStatus(GoodsStatus.ON));
        dto.setGoodsRejected(countGoodsByStatus(GoodsStatus.REJECTED));
        dto.setOrderCount(count("SELECT COUNT(*) FROM orders"));
        dto.setPaidOrderCount(count("SELECT COUNT(*) FROM orders WHERE status IN (1,2,3)"));
        BigDecimal gmv = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount),0) FROM orders WHERE status IN (1,2,3)", BigDecimal.class);
        dto.setTotalGmv(gmv != null ? gmv : BigDecimal.ZERO);
        dto.setTodayNewUsers(count("SELECT COUNT(*) FROM user WHERE DATE(created_at)=CURDATE()"));
        dto.setTodayNewGoods(count("SELECT COUNT(*) FROM goods WHERE DATE(created_at)=CURDATE()"));
        dto.setTodayNewOrders(count("SELECT COUNT(*) FROM orders WHERE DATE(created_at)=CURDATE()"));
        dto.setOrdersByStatus(queryStatusMap("orders", "status"));
        dto.setGoodsByStatus(queryStatusMap("goods", "status"));
        dto.setTrend7d(buildTrend7d());
        return dto;
    }

    @Override
    public PageResult<AdminGoodsDto> listGoods(Integer status, String keyword, long page, long size) {
        long current = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Goods::getTitle, keyword.trim());
        }
        wrapper.orderByDesc(Goods::getId);
        Page<Goods> pageResult = goodsServiceImpl.page(new Page<>(current, pageSize), wrapper);
        List<AdminGoodsDto> list = pageResult.getRecords().stream().map(this::toAdminGoodsDto).collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal(), current, pageSize);
    }

    @Override
    @Transactional
    public void auditGoods(Long adminId, Long goodsId, boolean approved, String reason) {
        Goods goods = goodsService.getByIdIncludeAll(goodsId);
        if (goods == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (goods.getStatus() == null || goods.getStatus() != GoodsStatus.PENDING) {
            throw new IllegalArgumentException("该商品不在待审核状态");
        }
        Date now = new Date();
        goods.setReviewedAt(now);
        goods.setReviewedBy(adminId);
        if (approved) {
            goods.setStatus(GoodsStatus.ON);
            goods.setRejectReason(null);
        } else {
            goods.setStatus(GoodsStatus.REJECTED);
            goods.setRejectReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : "不符合平台规范");
        }
        goods.setUpdatedAt(now);
        goodsService.updateGoods(goods);
    }

    @Override
    public PageResult<AdminUserDto> listUsers(String keyword, long page, long size) {
        long current = Math.max(page, 1);
        long pageSize = Math.min(Math.max(size, 1), 100);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw)
                    .or().like(User::getPhone, kw)
                    .or().like(User::getNickname, kw)
                    .or().like(User::getCampus, kw));
        }
        wrapper.orderByDesc(User::getId);
        Page<User> pageResult = userService.page(new Page<>(current, pageSize), wrapper);
        List<AdminUserDto> list = pageResult.getRecords().stream().map(user -> {
            AdminUserDto dto = new AdminUserDto();
            user.setPassword(null);
            dto.setUser(user);
            dto.setGoodsCount(goodsServiceImpl.lambdaQuery().eq(Goods::getSellerId, user.getId()).count());
            dto.setOrderCount(ordersServiceImpl.lambdaQuery()
                    .and(w -> w.eq(Orders::getBuyerId, user.getId()).or().eq(Orders::getSellerId, user.getId()))
                    .count());
            return dto;
        }).collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal(), current, pageSize);
    }

    @Override
    @Transactional
    public void updateUserRole(Long operatorId, Long userId, Integer role) {
        if (role == null || (role != UserRole.USER && role != UserRole.ADMIN)) {
            throw new IllegalArgumentException("角色无效");
        }
        User target = userService.getById(userId);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (operatorId.equals(userId) && role != UserRole.ADMIN) {
            throw new IllegalArgumentException("不能取消自己的管理员权限");
        }
        if (target.getRole() != null && target.getRole() == UserRole.ADMIN && role == UserRole.USER) {
            long adminCount = userService.lambdaQuery().eq(User::getRole, UserRole.ADMIN).count();
            if (adminCount <= 1) {
                throw new IllegalArgumentException("至少保留一名管理员");
            }
        }
        target.setRole(role);
        target.setUpdatedAt(new Date());
        userService.updateById(target);
    }

    @Override
    @Transactional
    public void updateUserAccountStatus(Long operatorId, Long userId, Integer accountStatus) {
        if (accountStatus == null || (accountStatus != AccountStatus.ACTIVE && accountStatus != AccountStatus.DISABLED)) {
            throw new IllegalArgumentException("账号状态无效");
        }
        User target = userService.getById(userId);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (operatorId.equals(userId)) {
            throw new IllegalArgumentException("不能禁用自己");
        }
        if (target.getRole() != null && target.getRole() == UserRole.ADMIN && accountStatus == AccountStatus.DISABLED) {
            throw new IllegalArgumentException("不能禁用管理员账号");
        }
        target.setAccountStatus(accountStatus);
        target.setUpdatedAt(new Date());
        userService.updateById(target);
    }

    private AdminGoodsDto toAdminGoodsDto(Goods goods) {
        AdminGoodsDto dto = new AdminGoodsDto();
        dto.setGoods(goods);
        User seller = userService.getById(goods.getSellerId());
        if (seller != null) {
            dto.setSellerUsername(seller.getUsername());
            dto.setSellerCampus(seller.getCampus());
        }
        return dto;
    }

    private long count(String sql) {
        Long val = jdbcTemplate.queryForObject(sql, Long.class);
        return val != null ? val : 0L;
    }

    private long countGoodsByStatus(int status) {
        Long val = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM goods WHERE status = ?", Long.class, status);
        return val != null ? val : 0L;
    }

    private Map<String, Long> queryStatusMap(String table, String column) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT " + column + " AS s, COUNT(*) AS c FROM " + table + " GROUP BY " + column);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object status = row.get("s");
            Object count = row.get("c");
            result.put(String.valueOf(status), count instanceof Number ? ((Number) count).longValue() : 0L);
        }
        return result;
    }

    private List<DailyTrendPoint> buildTrend7d() {
        Map<String, Long> usersByDay = queryDailyCount("user", "created_at");
        Map<String, Long> ordersByDay = queryDailyCount("orders", "created_at");
        Map<String, Long> goodsByDay = queryDailyCount("goods", "created_at");
        Map<String, BigDecimal> gmvByDay = queryDailyGmv();
        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM-dd");
        List<DailyTrendPoint> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String key = day.toString();
            DailyTrendPoint point = new DailyTrendPoint();
            point.setDate(day.format(labelFmt));
            point.setNewUsers(usersByDay.getOrDefault(key, 0L));
            point.setNewOrders(ordersByDay.getOrDefault(key, 0L));
            point.setNewGoods(goodsByDay.getOrDefault(key, 0L));
            point.setGmv(gmvByDay.getOrDefault(key, BigDecimal.ZERO));
            list.add(point);
        }
        return list;
    }

    private Map<String, Long> queryDailyCount(String table, String dateColumn) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATE(" + dateColumn + ") AS d, COUNT(*) AS c FROM " + table
                        + " WHERE " + dateColumn + " >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)"
                        + " GROUP BY DATE(" + dateColumn + ")");
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object day = row.get("d");
            Object count = row.get("c");
            if (day != null) {
                result.put(normalizeDayKey(day), count instanceof Number ? ((Number) count).longValue() : 0L);
            }
        }
        return result;
    }

    private Map<String, BigDecimal> queryDailyGmv() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT DATE(created_at) AS d, COALESCE(SUM(amount), 0) AS g FROM orders"
                        + " WHERE status IN (1,2,3) AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)"
                        + " GROUP BY DATE(created_at)");
        Map<String, BigDecimal> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object day = row.get("d");
            Object gmv = row.get("g");
            if (day != null) {
                String key = normalizeDayKey(day);
                if (gmv instanceof BigDecimal) {
                    result.put(key, (BigDecimal) gmv);
                } else if (gmv instanceof Number) {
                    result.put(key, BigDecimal.valueOf(((Number) gmv).doubleValue()));
                } else {
                    result.put(key, BigDecimal.ZERO);
                }
            }
        }
        return result;
    }

    private String normalizeDayKey(Object day) {
        if (day == null) {
            return "";
        }
        String s = String.valueOf(day);
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }
}

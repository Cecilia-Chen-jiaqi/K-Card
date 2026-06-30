package com.kpoptrade.controller;

import com.kpoptrade.constant.AccountStatus;
import com.kpoptrade.constant.GoodsStatus;
import com.kpoptrade.dto.AdminGoodsDto;
import com.kpoptrade.dto.AdminStatsDto;
import com.kpoptrade.dto.AdminUserDto;
import com.kpoptrade.dto.GoodsAuditDto;
import com.kpoptrade.dto.PageResult;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.AdminService;
import com.kpoptrade.util.AdminAuth;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private AdminAuth adminAuth;

    @GetMapping("/stats/overview")
    public R<AdminStatsDto> statsOverview() {
        try {
            adminAuth.requireAdmin();
            return R.ok(adminService.getOverviewStats());
        } catch (AdminAuth.AdminAccessException e) {
            return R.error(403, e.getMessage());
        }
    }

    @GetMapping("/goods/pending")
    public R<PageResult<AdminGoodsDto>> pendingGoods(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        try {
            adminAuth.requireAdmin();
            return R.ok(adminService.listGoods(GoodsStatus.PENDING, null, page, size));
        } catch (AdminAuth.AdminAccessException e) {
            return R.error(403, e.getMessage());
        }
    }

    @GetMapping("/goods")
    public R<PageResult<AdminGoodsDto>> listGoods(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        try {
            adminAuth.requireAdmin();
            return R.ok(adminService.listGoods(status, keyword, page, size));
        } catch (AdminAuth.AdminAccessException e) {
            return R.error(403, e.getMessage());
        }
    }

    @PostMapping("/goods/audit")
    public R<String> auditGoods(@RequestBody GoodsAuditDto dto) {
        try {
            User admin = adminAuth.requireAdmin();
            if (dto.getGoodsId() == null || dto.getApproved() == null) {
                return R.error("参数不完整");
            }
            adminService.auditGoods(admin.getId(), dto.getGoodsId(), dto.getApproved(), dto.getReason());
            return R.ok(dto.getApproved() ? "已通过审核" : "已拒绝");
        } catch (AdminAuth.AdminAccessException e) {
            return R.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/users")
    public R<PageResult<AdminUserDto>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        try {
            adminAuth.requireAdmin();
            return R.ok(adminService.listUsers(keyword, page, size));
        } catch (AdminAuth.AdminAccessException e) {
            return R.error(403, e.getMessage());
        }
    }

    @PutMapping("/users/{id}/role")
    public R<String> updateRole(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            User admin = adminAuth.requireAdmin();
            Integer role = body.get("role");
            adminService.updateUserRole(admin.getId(), id, role);
            return R.ok("角色已更新");
        } catch (AdminAuth.AdminAccessException e) {
            return R.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }

    @PutMapping("/users/{id}/status")
    public R<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            User admin = adminAuth.requireAdmin();
            Integer accountStatus = body.get("accountStatus");
            adminService.updateUserAccountStatus(admin.getId(), id, accountStatus);
            return R.ok("账号状态已更新");
        } catch (AdminAuth.AdminAccessException e) {
            return R.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        }
    }
}

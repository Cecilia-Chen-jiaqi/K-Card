package com.kpoptrade.service;

import com.kpoptrade.dto.AdminGoodsDto;
import com.kpoptrade.dto.AdminStatsDto;
import com.kpoptrade.dto.AdminUserDto;
import com.kpoptrade.dto.PageResult;
import com.kpoptrade.entity.User;

public interface AdminService {
    AdminStatsDto getOverviewStats();

    PageResult<AdminGoodsDto> listGoods(Integer status, String keyword, long page, long size);

    void auditGoods(Long adminId, Long goodsId, boolean approved, String reason);

    PageResult<AdminUserDto> listUsers(String keyword, long page, long size);

    void updateUserRole(Long operatorId, Long userId, Integer role);

    void updateUserAccountStatus(Long operatorId, Long userId, Integer accountStatus);
}

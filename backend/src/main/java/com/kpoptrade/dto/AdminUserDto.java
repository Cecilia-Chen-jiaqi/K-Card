package com.kpoptrade.dto;

import com.kpoptrade.entity.User;
import lombok.Data;

@Data
public class AdminUserDto {
    private User user;
    private long goodsCount;
    private long orderCount;
}

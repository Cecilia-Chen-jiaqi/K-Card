package com.kpoptrade.util;

import com.kpoptrade.constant.UserRole;
import com.kpoptrade.entity.User;
import com.kpoptrade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminAuth {
    @Autowired
    private UserService userService;

    public User requireAdmin() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new AdminAccessException("请先登录");
        }
        User user = userService.getById(userId);
        if (user == null || user.getRole() == null || user.getRole() != UserRole.ADMIN) {
            throw new AdminAccessException("无管理员权限");
        }
        return user;
    }

    public static class AdminAccessException extends RuntimeException {
        public AdminAccessException(String message) {
            super(message);
        }
    }
}

package com.kpoptrade.service;

import com.kpoptrade.entity.User;

public interface UserService {
    User register(User user);
    User findByUsername(String username);
    User getById(Long id);
}

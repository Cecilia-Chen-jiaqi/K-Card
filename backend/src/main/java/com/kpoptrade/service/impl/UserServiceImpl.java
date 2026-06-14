package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.entity.User;
import com.kpoptrade.mapper.UserMapper;
import com.kpoptrade.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public User register(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return null;
        }
        User existing = lambdaQuery().eq(User::getUsername, user.getUsername()).one();
        if (existing != null) {
            return null;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(0);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        save(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public User findByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public User getById(Long id) {
        return super.getById(id);
    }
}

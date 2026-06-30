package com.kpoptrade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kpoptrade.constant.AccountStatus;
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
        if (user == null || user.getUsername() == null || user.getPassword() == null || user.getPhone() == null) {
            return null;
        }
        User existingByUsername = lambdaQuery().eq(User::getUsername, user.getUsername()).one();
        if (existingByUsername != null) {
            return null;
        }
        User existingByPhone = lambdaQuery().eq(User::getPhone, user.getPhone()).one();
        if (existingByPhone != null) {
            return null;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(0);
        user.setAccountStatus(AccountStatus.ACTIVE);
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
    public User findByPhone(String phone) {
        return lambdaQuery().eq(User::getPhone, phone).one();
    }

    @Override
    public User getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, User profile) {
        User user = getById(userId);
        if (user == null) {
            return null;
        }
        if (profile.getNickname() != null) {
            user.setNickname(profile.getNickname().trim());
        }
        if (profile.getCampus() != null && !profile.getCampus().trim().isEmpty()) {
            user.setCampus(profile.getCampus().trim());
        }
        if (profile.getIntro() != null) {
            user.setIntro(profile.getIntro().trim());
        }
        if (profile.getAvatar() != null && !profile.getAvatar().trim().isEmpty()) {
            user.setAvatar(profile.getAvatar().trim());
        }
        user.setUpdatedAt(new Date());
        updateById(user);
        user.setPassword(null);
        return user;
    }
}

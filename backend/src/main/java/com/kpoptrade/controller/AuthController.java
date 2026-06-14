package com.kpoptrade.controller;

import com.kpoptrade.entity.User;
import com.kpoptrade.service.UserService;
import com.kpoptrade.util.JwtUtil;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public R<User> register(@RequestBody User user) {
        User result = userService.register(user);
        if (result == null) {
            return R.error("用户名已存在或参数不完整");
        }
        return R.ok(result);
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || password == null) {
            return R.error("用户名和密码不能为空");
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            return R.error("用户名或密码错误");
        }
        String rawPassword = password;
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (!encoder.matches(rawPassword, user.getPassword())) {
            return R.error("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId());
        user.setPassword(null);
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("user", user);
        return R.ok(payload);
    }
}

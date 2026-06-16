package com.kpoptrade.controller;

import com.kpoptrade.entity.User;
import com.kpoptrade.service.UserService;
import com.kpoptrade.util.JwtUtil;
import com.kpoptrade.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<User> register(@RequestBody User user) {
        try {
            if (user == null
                    || user.getUsername() == null || user.getUsername().trim().isEmpty()
                    || user.getPassword() == null || user.getPassword().trim().isEmpty()
                    || user.getPhone() == null || user.getPhone().trim().isEmpty()
                    || user.getCampus() == null || user.getCampus().trim().isEmpty()) {
                return R.error("参数不完整，请检查用户名、密码、手机号和校园信息");
            }
            if (user.getPassword().length() < 6) {
                return R.error("密码长度至少6位");
            }
            if (userService.findByUsername(user.getUsername()) != null) {
                return R.error("用户名已存在");
            }
            if (userService.findByPhone(user.getPhone()) != null) {
                return R.error("手机号已被注册");
            }
            User result = userService.register(user);
            if (result == null) {
                return R.error("用户名或手机号已存在，请检查后重试");
            }
            result.setPassword(null);
            return R.ok(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return R.error("注册失败，请稍后重试: " + ex.getMessage());
        }
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return R.error("账号或密码错误");
        }
        User user = userService.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return R.error("账号或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId());
        user.setPassword(null);
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        payload.put("user", user);
        return R.ok(payload);
    }
}

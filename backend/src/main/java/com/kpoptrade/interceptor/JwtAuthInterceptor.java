package com.kpoptrade.interceptor;

import com.kpoptrade.util.JwtUtil;
import com.kpoptrade.util.LoginUserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/pay/success",
            "/api/pay/return",
            "/api/pay/notify",
            "/api/meta/kpop",
            "/api/meta/express"
    );

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (isPublicRequest(uri, request.getMethod())) {
            return true;
        }
        String token = jwtUtil.resolveToken(request.getHeader("Authorization"));
        if (token == null || !jwtUtil.validateToken(token)) {
            reject(response, 401, "Unauthorized");
            return false;
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            reject(response, 401, "Unauthorized");
            return false;
        }
        LoginUserHolder.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.clear();
    }

    private boolean isPublicRequest(String uri, String method) {
        if (PUBLIC_PATHS.contains(uri)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && isPublicGoodsRead(uri)) {
            return true;
        }
        if (uri.startsWith("/api/upload") && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (uri.startsWith("/api/pay/sync-status") && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        return uri.startsWith("/api/auth/");
    }

    /** 仅公开商品浏览类接口，/my 等需登录 */
    private boolean isPublicGoodsRead(String uri) {
        if (!uri.startsWith("/api/goods")) {
            return false;
        }
        if ("/api/goods/my".equals(uri)) {
            return false;
        }
        return uri.equals("/api/goods/list")
                || uri.startsWith("/api/goods/search")
                || uri.startsWith("/api/goods/detail")
                || uri.equals("/api/goods/campus")
                || uri.equals("/api/goods/exchange")
                || uri.matches("/api/goods/\\d+");
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(String.format("{\"code\":%d,\"message\":\"%s\"}", status, message));
    }
}

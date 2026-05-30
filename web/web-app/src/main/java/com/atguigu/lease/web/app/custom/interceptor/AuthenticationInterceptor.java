package com.atguigu.lease.web.app.custom.interceptor;

import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.web.app.custom.LoginUser;
import com.atguigu.lease.web.app.custom.holder.LoginUserHolder;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("access-token");

        // Debug: print token
        System.out.println("AuthenticationInterceptor - Token: " + token);

        Claims claims = JwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);

        // Debug: print extracted values
        System.out.println("AuthenticationInterceptor - UserId: " + userId + ", Username: " + username);

        LoginUserHolder.setLoginUser(new LoginUser(userId, username));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.clear();
    }
}
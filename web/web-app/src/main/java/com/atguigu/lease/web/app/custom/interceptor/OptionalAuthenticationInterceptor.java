package com.atguigu.lease.web.app.custom.interceptor;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选登录拦截器：与 {@link AuthenticationInterceptor}（强制校验）相对。
 * 请求头带合法 access-token 时解析出登录用户放入 LoginUserHolder；
 * 未登录或 token 无效则匿名放行，不拦截请求。
 *
 * 适用场景：游客可浏览、但登录与否行为不同的接口，
 * 如房源详情（/app/room/**，登录用户浏览时记录浏览历史）。
 */
@Component
public class OptionalAuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String token = request.getHeader("access-token");
            Claims claims = JwtUtil.parseToken(token);
            // 校验 token 用途：拒绝管理端 token 访问用户端（防跨端越权）
            JwtUtil.validateScope(claims, JwtUtil.SCOPE_APP);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            LoginUserHolder.setLoginUser(new LoginUser(userId, username));
        } catch (Exception e) {
            // 未登录或 token 无效：匿名放行，不设置登录用户
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.clear();
    }
}

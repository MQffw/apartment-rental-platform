package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private JwtProperties jwtProperties;

    private static SecretKey staticTokenSignKey;
    private static long staticTokenExpiration;

    @PostConstruct
    public void init() {
        // fail-fast：密钥缺失或长度不足时启动即失败，而非运行期 NPE
        if (jwtProperties.getSecret() == null
                || jwtProperties.getSecret().getBytes().length < 32) {
            throw new IllegalStateException(
                    "jwt.secret 未配置或长度不足32字节（HS256要求），请通过配置或 JWT_SECRET 环境变量注入");
        }
        staticTokenSignKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        staticTokenExpiration = jwtProperties.getExpiration();
    }

    /** token 用途声明：管理端 */
    public static final String SCOPE_ADMIN = "admin";
    /** token 用途声明：用户端 */
    public static final String SCOPE_APP = "app";

    public static String createToken(Long userId, String username, String scope) {
        var builder = Jwts.builder()
                .setSubject("USER_INFO")
                .setExpiration(new Date(System.currentTimeMillis() + staticTokenExpiration))
                .claim("userId", userId)
                .claim("username", username)
                .claim("scope", scope);
        return builder.signWith(staticTokenSignKey).compact();
    }

    /**
     * 校验 token 用途是否匹配。双端共享同一签名密钥，若不区分 scope，
     * 用户端 token 可通过管理端认证（user_info 与 system_user 独立自增，
     * id 撞号时普通用户即等同于管理员），因此跨端 token 必须拒绝。
     */
    public static void validateScope(Claims claims, String expectedScope) {
        String scope = claims.get("scope", String.class);
        if (!expectedScope.equals(scope)) {
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }

    public static Claims parseToken(String token) {

        if (token == null) {
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }

        try {
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(staticTokenSignKey).build();
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (ExpiredJwtException e) {
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }
}

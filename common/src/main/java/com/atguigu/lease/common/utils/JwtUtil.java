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
        staticTokenSignKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        staticTokenExpiration = jwtProperties.getExpiration();
    }

    public static String createToken(Long userId, String username) {
        return createToken(userId, username, null);
    }

    public static String createToken(Long userId, String username, Integer userType) {
        var builder = Jwts.builder()
                .setSubject("USER_INFO")
                .setExpiration(new Date(System.currentTimeMillis() + staticTokenExpiration))
                .claim("userId", userId)
                .claim("username", username);
        if (userType != null) {
            builder.claim("userType", userType);
        }
        return builder.signWith(staticTokenSignKey).compact();
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

package com.atguigu.lease.common.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置。
 * secret 不设默认值：必须通过配置注入（dev 在 application-dev.yml，prod 走 JWT_SECRET 环境变量），
 * 避免签名密钥硬编码进源码/Git 历史。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 签名密钥，长度必须 >= 32 字节 */
    private String secret;

    /** token 有效期（毫秒），默认 24 小时 */
    private long expiration = 24 * 60 * 60 * 1000L;
}

package com.atguigu.lease.common.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ";

    private long expiration = 24 * 60 * 60 * 1000L;
}

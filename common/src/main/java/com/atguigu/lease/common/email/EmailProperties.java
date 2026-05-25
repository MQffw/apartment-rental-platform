package com.atguigu.lease.common.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "verify.code")
public class EmailProperties {

    private Integer length = 6;

    private Integer ttl = 300;

    private Integer resend = 60;
}
package com.atguigu.lease.common.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解（基于 Redis 计数，按登录用户或客户端 IP 维度）。
 * 用法示例：@RateLimit(max = 1, window = 60) 表示 60 秒内最多调用 1 次。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 时间窗口内允许的最大调用次数 */
    int max() default 1;

    /** 时间窗口（秒） */
    int window() default 60;

    /** 触发限流时的提示信息 */
    String message() default "操作太频繁，请稍后再试";
}

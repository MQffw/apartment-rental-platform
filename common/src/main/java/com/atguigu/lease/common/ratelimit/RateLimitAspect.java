package com.atguigu.lease.common.ratelimit;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * @RateLimit 注解切面：Redis INCR + TTL 实现固定窗口计数限流。
 * 维度优先级：登录用户ID > 客户端IP（未登录接口）。
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String key = RedisConstant.RATE_LIMIT_PREFIX + resolveIdentity()
                + ":" + pjp.getSignature().toShortString();
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, rateLimit.window(), TimeUnit.SECONDS);
        }
        if (count != null && count > rateLimit.max()) {
            log.warn("接口限流触发: key={}, count={}, max={}", key, count, rateLimit.max());
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, rateLimit.message());
        }
        return pjp.proceed();
    }

    /** 限流主体：登录用户ID，未登录回退到客户端IP */
    private String resolveIdentity() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser != null && loginUser.getUserId() != null) {
            return String.valueOf(loginUser.getUserId());
        }
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String ip = attrs.getRequest().getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = attrs.getRequest().getRemoteAddr();
            }
            if (ip != null && !ip.isBlank()) {
                return "ip:" + ip.split(",")[0].trim();
            }
        }
        return "anonymous";
    }
}

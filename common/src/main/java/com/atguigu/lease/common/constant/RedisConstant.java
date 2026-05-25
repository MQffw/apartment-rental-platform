package com.atguigu.lease.common.constant;

public class RedisConstant {

    public static final String ADMIN_LOGIN_PREFIX = "admin:login:";
    public static final Integer ADMIN_LOGIN_CAPTCHA_TTL_SEC = 60;

    public static final String APP_LOGIN_PREFIX = "app:login:";
    public static final Integer APP_LOGIN_CODE_RESEND_TIME_SEC = 60;
    public static final Integer APP_LOGIN_CODE_TTL_SEC = 60 * 10;

    public static final String APP_ROOM_PREFIX = "app:room:";

    // 登录验证码前缀
    public static final String APP_LOGIN_EMAIL_PREFIX = "app:login:email:";

    // 验证码有效期（秒）
    public static final Integer APP_LOGIN_EMAIL_CODE_TTL_SEC = 300;

    // 验证码重发间隔（秒）
    public static final Integer APP_LOGIN_EMAIL_CODE_RESEND_TIME_SEC = 60;
}

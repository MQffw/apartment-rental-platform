package com.atguigu.lease.common.constant;

public class RedisConstant {

    public static final String ADMIN_LOGIN_PREFIX = "admin:login:";
    public static final Integer ADMIN_LOGIN_CAPTCHA_TTL_SEC = 60;

    public static final String APP_LOGIN_PREFIX = "app:login:";
    public static final Integer APP_LOGIN_CODE_RESEND_TIME_SEC = 60;
    public static final Integer APP_LOGIN_CODE_TTL_SEC = 60 * 10;

    public static final String APP_ROOM_PREFIX = "app:room:";
    public static final Integer APP_ROOM_CACHE_TTL_SEC = 30 * 60; // 30分钟

    // 登录验证码前缀
    public static final String APP_LOGIN_EMAIL_PREFIX = "app:login:email:";

    // 验证码有效期（秒）
    public static final Integer APP_LOGIN_EMAIL_CODE_TTL_SEC = 300;

    // 验证码重发间隔（秒）
    public static final Integer APP_LOGIN_EMAIL_CODE_RESEND_TIME_SEC = 60;

    // ==================== 通知未读数 ====================
    public static final String NOTIFICATION_UNREAD_PREFIX = "notification:unread:";

    // ==================== 帖子点赞 ====================
    public static final String POST_LIKE_USERS_PREFIX = "post:like:";
    public static final String POST_LIKE_USERS_SUFFIX = ":users";
    public static final String POST_LIKE_COUNT_SUFFIX = ":count";

    // ==================== 帖子评论数 ====================
    public static final String POST_COMMENT_COUNT_PREFIX = "post:comment:";
    public static final String POST_COMMENT_COUNT_SUFFIX = ":count";

    // ==================== 帖子缓存 ====================
    public static final String POST_DETAIL_PREFIX = "post:detail:";
    public static final Integer POST_DETAIL_CACHE_TTL_SEC = 5 * 60;
    public static final String POST_LIST_PREFIX = "post:list:";
    public static final Integer POST_LIST_CACHE_TTL_SEC = 5 * 60;
    public static final String POST_LIST_LOCK_PREFIX = "lock:post:list:";

    // ==================== 热门排行 ====================
    public static final String POST_HOT_RANK = "post:hot:rank";

    // ==================== 限流 ====================
    public static final String RATE_LIMIT_PREFIX = "rate:limit:";
}

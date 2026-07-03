package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType implements BaseEnum {

    LEASE_EXPIRY(1, "租约到期提醒"),
    APPOINTMENT_CHANGE(2, "预约状态变更"),
    NEW_APPOINTMENT(3, "新预约通知"),
    LEASE_CHANGE(4, "租约状态变更"),
    PAYMENT_REMINDER(5, "缴费提醒"),
    POST_LIKE(6, "帖子被赞"),
    POST_COMMENT(7, "帖子被评论");

    @EnumValue
    @JsonValue
    private Integer code;
    private String name;

    NotificationType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() { return this.code; }

    @Override
    public String getName() { return this.name; }
}

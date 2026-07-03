package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostStatus implements BaseEnum {

    NORMAL(1, "正常"),
    REVIEWING(2, "审核中"),
    BLOCKED(3, "已屏蔽");

    @EnumValue
    @JsonValue
    private Integer code;
    private String name;

    PostStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() { return this.code; }

    @Override
    public String getName() { return this.name; }
}

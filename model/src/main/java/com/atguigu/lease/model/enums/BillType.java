package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BillType implements BaseEnum {

    RENT(1, "房租"),
    ELECTRICITY(2, "电费"),
    WATER(3, "水费"),
    PROPERTY(4, "物业费");

    @EnumValue
    @JsonValue
    private Integer code;
    private String name;

    BillType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() { return this.code; }

    @Override
    public String getName() { return this.name; }
}

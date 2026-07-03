package com.atguigu.lease.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus implements BaseEnum {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    FAILED(2, "支付失败");

    @EnumValue
    @JsonValue
    private Integer code;

    private String name;

    PaymentStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class LeaseException extends RuntimeException {

    private Integer code;

    // 1. 原有构造方法
    public LeaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    // 2. 只传枚举（最常用）
    public LeaseException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    // 3. 传枚举 + 自定义消息（覆盖枚举的默认消息）
    public LeaseException(ResultCodeEnum resultCodeEnum, String message) {
        super(message);
        this.code = resultCodeEnum.getCode();
    }

    // 4. 传枚举 + 异常原因（用于异常链）
    public LeaseException(ResultCodeEnum resultCodeEnum, Throwable cause) {
        super(resultCodeEnum.getMessage(), cause);
        this.code = resultCodeEnum.getCode();
    }
}
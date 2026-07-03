package com.atguigu.lease.common.exception;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LeaseException.class)
    @ResponseBody
    public Result<Void> handle(LeaseException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result<Void> handle(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<Void> handle(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.fail(ResultCodeEnum.SERVICE_ERROR.getCode(), "系统繁忙，请稍后重试");
    }
}

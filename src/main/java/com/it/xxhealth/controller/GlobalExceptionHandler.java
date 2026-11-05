package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class OrdersGlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return Result.error(400, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.error(500, "服务器处理失败：" + exception.getMessage());
    }
}

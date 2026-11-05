package com.it.xxhealth.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一包装为 Result，避免接口直接抛出 500 页面。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("接口执行异常", e);
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return Result.error("操作失败：" + message);
    }
}

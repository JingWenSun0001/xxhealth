package com.it.xxhealth.controller;

import com.it.xxhealth.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * OrdersGlobalExceptionHandler实现类。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestControllerAdvice
class OrdersGlobalExceptionHandler {

    /**
     * 处理当前业务请求并返回执行结果。
     * @param exception exception 参数
     * @return 方法处理结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return Result.error(400, exception.getMessage());
    }

    /**
     * 捕获未被业务异常处理器处理的异常并记录错误日志。
     * @param exception exception 参数
     * @return 方法处理结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.error(500, "服务器处理失败：" + exception.getMessage());
    }
}

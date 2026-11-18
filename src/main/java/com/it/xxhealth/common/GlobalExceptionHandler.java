package com.it.xxhealth.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，将服务端异常转换为可识别的接口响应。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 用于记录服务端异常信息的日志组件。
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获未被业务异常处理器处理的异常并记录错误日志。
     * @param e 捕获到的异常
     * @return 方法处理结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("接口执行异常", e);
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return Result.error("操作失败：" + message);
    }
}

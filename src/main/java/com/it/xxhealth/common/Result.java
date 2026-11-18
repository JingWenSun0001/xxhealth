package com.it.xxhealth.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 后端接口统一响应对象，封装业务状态码、提示信息和响应数据。
 *
 * 创建日期：2026-11-16
 *
 * @author 熙心健康项目组
 * @since 2026-11-16
 */
@Data
public class Result<T> implements Serializable {
    /**
     * 序列化版本号。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码。
     */
    private int code;

    /**
     * 面向调用方的结果说明。
     */
    private String message;

    /**
     * 接口返回的业务数据。
     */
    private T data;

    /**
     * 私有构造方法，防止外部直接 new 对象
     */
    private Result() {}

    /**
     * 构造操作成功的统一响应。
     * @return 方法处理结果
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 构造操作成功的统一响应。
     * @param data 需要返回的业务数据
     * @return 方法处理结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 构造操作失败的统一响应。
     * @param message 面向调用方的提示信息
     * @return 方法处理结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 构造操作失败的统一响应。
     * @param code 业务状态码
     * @param message 面向调用方的提示信息
     * @return 方法处理结果
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
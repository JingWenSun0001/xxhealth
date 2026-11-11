package com.it.xxhealth.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 全局统一返回结果类
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 状态码 (例如：200 成功, 500 失败)
    private int code;

    // 提示信息
    private String message;

    // 返回的数据
    private T data;

    /**
     * 私有构造方法，防止外部直接 new 对象
     */
    private Result() {}

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 失败返回（默认错误码 500）
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败返回（自定义错误码和提示）
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
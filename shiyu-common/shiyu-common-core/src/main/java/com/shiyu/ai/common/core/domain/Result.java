package com.shiyu.ai.common.core.domain;

import com.shiyu.ai.common.core.enums.BizResultCode;
import lombok.Data;

@Data
public class Result<T> {
    /**
     * 查询数据
     */
    private T data;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 描述
     */
    private String message;

    /**
     * 描述
     */
    private boolean success;

    public static <T> Result<T> common(T data, Integer code, String message, boolean success) {
        Result<T> result = new Result<>();
        result.setData(data);
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(success);
        return result;
    }

    public static <T> Result<T> success(BizResultCode resultCode) {
        return common(null, resultCode.getCode(), resultCode.getMsg(), true);
    }

    public static <T> Result<T> success(BizResultCode resultCode, T data) {
        return common(data, resultCode.getCode(), resultCode.getMsg(), true);
    }

    public static <T> Result<T> fail(BizResultCode resultCode) {
        return common(null, resultCode.getCode(), resultCode.getMsg(), false);
    }

    public static <T> Result<T> fail(BizResultCode resultCode, T data) {
        return common(data, resultCode.getCode(), resultCode.getMsg(), false);
    }

    public static <T> Result<T> fail(BizResultCode resultCode, String message) {
        return common(null, resultCode.getCode(), message, false);
    }

    public static <T> Result<T> success() {
        return success(BizResultCode.SUC);
    }

    public static <T> Result<T> success(T data) {
        return success(BizResultCode.SUC, data);
    }

    public static <T> Result<T> fail() {
        return fail(BizResultCode.ERR);
    }

    public static <T> Result<T> fail(T data) {
        return fail(BizResultCode.ERR, data);
    }
    public static <T> Result<T> fail(String  message) {
        return fail(BizResultCode.ERR, message);
    }
}

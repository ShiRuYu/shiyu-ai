package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.BizResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /**
     * 响应码
     */
    private Integer code;

    /**
     * 查询数据
     */
    private T data;

    /**
     * 描述
     */
    private String message;

    /**
     * 错误信息（与 message 内容一致时不再冗余存储）
     */
    private String error;

    /**
     * 是否成功
     */
    private boolean success;

    public static <T> Result<T> common(T data, Integer code, String message, String error, boolean success) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setError(error);
        result.setMessage(message);
        result.setSuccess(success);
        return result;
    }

    public static <T> Result<T> common(T data, Integer code, String message, boolean success) {
        return common(data, code, message, null, success);
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
        return common(null, resultCode.getCode(), message, null, false);
    }

    public static <T> Result<T> success() {
        return success(BizResultCode.SUC);
    }

    public static <T> Result<T> success(T data) {
        return success(BizResultCode.SUC, data);
    }

    public static <T> Result<T> fail() {
        return fail(BizResultCode.ERROR);
    }

    public static <T> Result<T> fail(T data) {
        return fail(BizResultCode.ERROR, data);
    }
    public static <T> Result<T> fail(String  message) {
        return fail(BizResultCode.ERROR, message);
    }
}

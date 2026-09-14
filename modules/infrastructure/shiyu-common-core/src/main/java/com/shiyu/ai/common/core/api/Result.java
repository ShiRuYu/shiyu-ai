package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.BizResultCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code Result} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /** 响应码 */
    private Integer code;

    /** 查询数据 */
    private T data;

    /** 描述 */
    private String message;

    /** 错误信息（与 message 内容一致时不再冗余存储） */
    private String error;

    /** 是否成功 */
    private boolean success;

    /**
     * {@code common} 执行当前类型定义的业务操作。
     *
     * @param data 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     * @param error 参数值，用于执行当前操作。
     * @param success 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> common(
            T data, Integer code, String message, String error, boolean success) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setError(error);
        result.setMessage(message);
        result.setSuccess(success);
        return result;
    }

    /**
     * {@code common} 执行当前类型定义的业务操作。
     *
     * @param data 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     * @param success 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> common(T data, Integer code, String message, boolean success) {
        return common(data, code, message, null, success);
    }

    /**
     * {@code success} 执行当前类型定义的业务操作。
     *
     * @param resultCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> success(BizResultCode resultCode) {
        return common(null, resultCode.getCode(), resultCode.getMsg(), true);
    }

    /**
     * {@code success} 执行当前类型定义的业务操作。
     *
     * @param resultCode 参数值，用于执行当前操作。
     * @param data 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> success(BizResultCode resultCode, T data) {
        return common(data, resultCode.getCode(), resultCode.getMsg(), true);
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param resultCode 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> fail(BizResultCode resultCode) {
        return common(null, resultCode.getCode(), resultCode.getMsg(), false);
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param resultCode 参数值，用于执行当前操作。
     * @param data 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> fail(BizResultCode resultCode, T data) {
        return common(data, resultCode.getCode(), resultCode.getMsg(), false);
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param resultCode 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> fail(BizResultCode resultCode, String message) {
        return common(null, resultCode.getCode(), message, null, false);
    }

    /**
     * {@code success} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> success() {
        return success(BizResultCode.SUC);
    }

    /**
     * {@code success} 执行当前类型定义的业务操作。
     *
     * @param data 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> success(T data) {
        return success(BizResultCode.SUC, data);
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> fail() {
        return fail(BizResultCode.ERROR);
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param data 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> fail(T data) {
        return fail(BizResultCode.ERROR, data);
    }

    /**
     * {@code fail} 执行当前类型定义的业务操作。
     *
     * @param message 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static <T> Result<T> fail(String message) {
        return fail(BizResultCode.ERR_10009, message);
    }
}

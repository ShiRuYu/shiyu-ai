package com.shiyu.ai.common.core.api;

import com.shiyu.ai.common.core.enums.BizResultCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示 Result 相关流程中的状态、关系或执行数据。
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
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param data 用于完成本次业务处理的 data 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param message 本次流程携带的事件或业务数据。
     * @param error 用于完成本次业务处理的 error 参数。
     * @param success 用于完成本次业务处理的 success 参数。
     * @return 返回 Result 相关操作生成的结果数据。
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
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param data 用于完成本次业务处理的 data 参数。
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param message 本次流程携带的事件或业务数据。
     * @param success 用于完成本次业务处理的 success 参数。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> common(T data, Integer code, String message, boolean success) {
        return common(data, code, message, null, success);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param resultCode 用于完成本次业务处理的 resultCode 参数。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> success(BizResultCode resultCode) {
        return common(null, resultCode.getCode(), resultCode.getMsg(), true);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param resultCode 用于完成本次业务处理的 resultCode 参数。
     * @param data 用于完成本次业务处理的 data 参数。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> success(BizResultCode resultCode, T data) {
        return common(data, resultCode.getCode(), resultCode.getMsg(), true);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param resultCode 用于完成本次业务处理的 resultCode 参数。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> fail(BizResultCode resultCode) {
        return common(null, resultCode.getCode(), resultCode.getMsg(), false);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param resultCode 用于完成本次业务处理的 resultCode 参数。
     * @param data 用于完成本次业务处理的 data 参数。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> fail(BizResultCode resultCode, T data) {
        return common(data, resultCode.getCode(), resultCode.getMsg(), false);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param resultCode 用于完成本次业务处理的 resultCode 参数。
     * @param message 本次流程携带的事件或业务数据。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> fail(BizResultCode resultCode, String message) {
        return common(null, resultCode.getCode(), message, null, false);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> success() {
        return success(BizResultCode.SUC);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param data 用于完成本次业务处理的 data 参数。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> success(T data) {
        return success(BizResultCode.SUC, data);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> fail() {
        return fail(BizResultCode.ERROR);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param data 用于完成本次业务处理的 data 参数。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> fail(T data) {
        return fail(BizResultCode.ERROR, data);
    }

    /**
     * 执行 Result 相关业务数据，并返回处理结果。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @return 返回 Result 相关操作生成的结果数据。
     */
    public static <T> Result<T> fail(String message) {
        return fail(BizResultCode.ERR_10009, message);
    }
}

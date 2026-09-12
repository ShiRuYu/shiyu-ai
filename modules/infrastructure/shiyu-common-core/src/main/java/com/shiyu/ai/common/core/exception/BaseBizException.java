package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/** 业务异常基类 提供 message、detailMessage、code 的统一管理 GlobalException 和 ServiceException 继承此类 */
public abstract class BaseBizException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 消息，表示当前对象中的对应属性。
     */
    private String message;

    /**
     * detailMessage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String detailMessage;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private Integer code;

    /**
     * {@code BaseBizException} 创建并初始化当前类型实例。
     */
    protected BaseBizException() {}

    /**
     * {@code BaseBizException} 创建并初始化当前类型实例。
     *
     * @param message 参数值，用于执行当前操作。
     */
    protected BaseBizException(String message) {
        this.message = message;
    }

    /**
     * {@code BaseBizException} 创建并初始化当前类型实例。
     *
     * @param message 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     */
    protected BaseBizException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    /**
     * {@code getDetailMessage} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getDetailMessage() {
        return detailMessage;
    }

    /**
     * {@code setDetailMessage} 写入或更新当前模块中的业务数据。
     *
     * @param detailMessage 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public BaseBizException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    /**
     * {@code getMessage} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * {@code setMessage} 写入或更新当前模块中的业务数据。
     *
     * @param message 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public BaseBizException setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * {@code getCode} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Integer getCode() {
        return code;
    }
}

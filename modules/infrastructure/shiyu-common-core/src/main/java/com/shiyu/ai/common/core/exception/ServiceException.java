package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/** 业务异常 */
public final class ServiceException extends BaseBizException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public ServiceException() {}

    /**
     * {@code ServiceException} 创建并初始化当前类型实例。
     *
     * @param message 参数值，用于执行当前操作。
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * {@code ServiceException} 创建并初始化当前类型实例。
     *
     * @param message 参数值，用于执行当前操作。
     * @param code 参数值，用于执行当前操作。
     */
    public ServiceException(String message, Integer code) {
        super(message, code);
    }

    /**
     * {@code setMessage} 写入或更新当前模块中的业务数据。
     *
     * @param message 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ServiceException setMessage(String message) {
        return (ServiceException) super.setMessage(message);
    }

    /**
     * {@code setDetailMessage} 写入或更新当前模块中的业务数据。
     *
     * @param detailMessage 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public ServiceException setDetailMessage(String detailMessage) {
        return (ServiceException) super.setDetailMessage(detailMessage);
    }
}

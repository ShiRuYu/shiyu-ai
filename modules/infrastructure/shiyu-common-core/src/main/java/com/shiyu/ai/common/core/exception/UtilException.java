package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/** 工具类异常 */
public class UtilException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 8247610319171014183L;

    public UtilException(Throwable e) {
        super(e.getMessage(), e);
    }

    /**
     * {@code UtilException} 创建并初始化当前类型实例。
     *
     * @param message 参数值，用于执行当前操作。
     */
    public UtilException(String message) {
        super(message);
    }

    /**
     * {@code UtilException} 创建并初始化当前类型实例。
     *
     * @param message 参数值，用于执行当前操作。
     * @param throwable 参数值，用于执行当前操作。
     */
    public UtilException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

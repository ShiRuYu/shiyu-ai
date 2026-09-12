package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/** 全局异常 */
public class GlobalException extends BaseBizException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public GlobalException() {}

    /**
     * {@code GlobalException} 创建并初始化当前类型实例。
     *
     * @param message 参数值，用于执行当前操作。
     */
    public GlobalException(String message) {
        super(message);
    }
}

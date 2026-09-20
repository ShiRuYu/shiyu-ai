package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/**
 * 表示 Global 相关的领域事件或异常信息。
 */
public class GlobalException extends BaseBizException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public GlobalException() {}

    /**
     * 执行 Global 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    public GlobalException(String message) {
        super(message);
    }
}

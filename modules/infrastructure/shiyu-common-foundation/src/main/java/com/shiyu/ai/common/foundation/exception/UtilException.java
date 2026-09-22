package com.shiyu.ai.common.foundation.exception;

import java.io.Serial;

/**
 * 表示 Util 相关的领域事件或异常信息。
 */
public class UtilException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 8247610319171014183L;

    public UtilException(Throwable e) {
        super(e.getMessage(), e);
    }

    /**
     * 执行 Util 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    public UtilException(String message) {
        super(message);
    }

    /**
     * 执行 Util 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     * @param throwable 用于完成本次业务处理的 throwable 参数。
     */
    public UtilException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

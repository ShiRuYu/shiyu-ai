package com.shiyu.ai.common.foundation.exception;

import java.io.Serial;

/**
 * 表示 Demo Mode 相关的领域事件或异常信息。
 */
public class DemoModeException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public DemoModeException() {}
}

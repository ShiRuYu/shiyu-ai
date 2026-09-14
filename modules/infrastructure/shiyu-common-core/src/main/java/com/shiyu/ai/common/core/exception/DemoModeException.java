package com.shiyu.ai.common.core.exception;

import java.io.Serial;

/** 演示模式异常 */
public class DemoModeException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    public DemoModeException() {}
}

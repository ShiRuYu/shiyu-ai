package com.shiyu.ai.kernel.error;

import java.util.Objects;

/**
 * DomainException 异常类型，表示共享内核领域相关业务或访问错误。
 */
public class DomainException extends RuntimeException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;

    /**
     * {@code DomainException} 创建并初始化当前类型实例。
     *
     * @param code 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     */
    public DomainException(String code, String message) {
        super(message);
        this.code = requireCode(code);
    }

    /**
     * {@code DomainException} 创建并初始化当前类型实例。
     *
     * @param code 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     * @param cause 参数值，用于执行当前操作。
     */
    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = requireCode(code);
    }

    /**
     * {@code code} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public final String code() {
        return code;
    }

    private static String requireCode(String code) {
        Objects.requireNonNull(code, "code must not be null");
        if (code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        return code;
    }
}

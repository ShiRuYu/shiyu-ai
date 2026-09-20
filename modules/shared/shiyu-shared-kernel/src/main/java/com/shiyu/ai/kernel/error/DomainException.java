package com.shiyu.ai.kernel.error;

import java.util.Objects;

/**
 * 表示 Domain 相关的领域事件或异常信息。
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
     * 执行 Domain 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param message 本次流程携带的事件或业务数据。
     */
    public DomainException(String code, String message) {
        super(message);
        this.code = requireCode(code);
    }

    /**
     * 执行 Domain 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param message 本次流程携带的事件或业务数据。
     * @param cause 用于完成本次业务处理的 cause 参数。
     */
    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = requireCode(code);
    }

    /**
     * 执行 Domain 相关业务数据，并返回处理结果。
     *
     * @return 返回 Domain 相关操作生成的结果数据。
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

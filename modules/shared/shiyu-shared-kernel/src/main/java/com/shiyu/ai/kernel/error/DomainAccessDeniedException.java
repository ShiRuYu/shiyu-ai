package com.shiyu.ai.kernel.error;

/**
 * DomainAccessDeniedException 异常类型，表示共享内核领域相关业务或访问错误。
 */
public final class DomainAccessDeniedException extends DomainException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@code DomainAccessDeniedException} 创建并初始化当前类型实例。
     *
     * @param code 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     */
    public DomainAccessDeniedException(String code, String message) {
        super(code, message);
    }
}

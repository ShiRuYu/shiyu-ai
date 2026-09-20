package com.shiyu.ai.kernel.error;

/**
 * 表示 Domain Access Denied 相关的领域事件或异常信息。
 */
public final class DomainAccessDeniedException extends DomainException {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;

    /**
     * 执行 Domain Access Denied 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param code 用于定位或筛选目标业务对象的业务值。
     * @param message 本次流程携带的事件或业务数据。
     */
    public DomainAccessDeniedException(String code, String message) {
        super(code, message);
    }
}

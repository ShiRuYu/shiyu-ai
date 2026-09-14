package com.shiyu.ai.conversation.implementation.application;

/**
 * GenerationAdmissionException 异常类型，表示会话领域相关业务或访问错误。
 */
public class GenerationAdmissionException extends IllegalStateException {
    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    private static final long serialVersionUID = 1L;
    /**
     * 错误编码，表示当前对象中的对应属性。
     */
    private final String errorCode;

    /**
     * {@code GenerationAdmissionException} 创建并初始化当前类型实例。
     *
     * @param errorCode 参数值，用于执行当前操作。
     */
    public GenerationAdmissionException(String errorCode) {
        super(errorCode == null || errorCode.isBlank() ? "generation admission denied" : errorCode);
        this.errorCode = errorCode;
    }

    /**
     * {@code errorCode} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public String errorCode() {
        return errorCode;
    }
}

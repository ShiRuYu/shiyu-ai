package com.shiyu.ai.conversation.implementation.application;

/**
 * 表示 生成 Admission 相关的领域事件或异常信息。
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
     * 执行 生成 Admission 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param errorCode 用于完成本次业务处理的 errorCode 参数。
     */
    public GenerationAdmissionException(String errorCode) {
        super(errorCode == null || errorCode.isBlank() ? "generation admission denied" : errorCode);
        this.errorCode = errorCode;
    }

    /**
     * 执行 生成 Admission 相关业务数据，并返回处理结果。
     *
     * @return 返回 生成 Admission 相关操作生成的结果数据。
     */
    public String errorCode() {
        return errorCode;
    }
}

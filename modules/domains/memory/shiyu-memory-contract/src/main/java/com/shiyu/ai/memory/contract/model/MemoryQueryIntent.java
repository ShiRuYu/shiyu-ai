package com.shiyu.ai.memory.contract.model;

/**
 * MemoryQueryIntent 枚举，定义记忆模块可用的业务取值。
 */
public enum MemoryQueryIntent {
    SEMANTIC,
    TEMPORAL,
    CAUSAL,
    ENTITY,
    HYBRID;

    /**
     * {@code infer} 执行当前类型定义的业务操作。
     *
     * @param text 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static MemoryQueryIntent infer(String text) {
        String value = text == null ? "" : text.toLowerCase(java.util.Locale.ROOT);
        if (value.matches(".*(why|because|cause|result|impact|导致|原因|影响|结果).*")) return CAUSAL;
        if (value.matches(".*(when|before|after|during|since|时间|之前|之后|何时|变化).*")) return TEMPORAL;
        if (value.matches(".*(who|which|about|related|关于|哪个|谁|对象).*")) return ENTITY;
        return SEMANTIC;
    }
}

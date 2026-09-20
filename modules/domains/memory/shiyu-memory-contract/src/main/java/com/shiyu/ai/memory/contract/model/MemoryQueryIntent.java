package com.shiyu.ai.memory.contract.model;

/**
 * 定义 记忆 Query Intent 可用的枚举值及其业务语义。
 */
public enum MemoryQueryIntent {
    SEMANTIC,
    TEMPORAL,
    CAUSAL,
    ENTITY,
    HYBRID;

    /**
     * 执行 记忆 Query Intent 相关业务数据，并返回处理结果。
     *
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 记忆 Query Intent 相关操作生成的结果数据。
     */
    public static MemoryQueryIntent infer(String text) {
        String value = text == null ? "" : text.toLowerCase(java.util.Locale.ROOT);
        if (value.matches(".*(why|because|cause|result|impact|导致|原因|影响|结果).*")) return CAUSAL;
        if (value.matches(".*(when|before|after|during|since|时间|之前|之后|何时|变化).*")) return TEMPORAL;
        if (value.matches(".*(who|which|about|related|关于|哪个|谁|对象).*")) return ENTITY;
        return SEMANTIC;
    }
}

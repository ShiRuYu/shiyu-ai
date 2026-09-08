package com.shiyu.ai.memory.magma;

/** The relation view a query is primarily asking for. */
public enum MemoryQueryIntent {
    SEMANTIC, TEMPORAL, CAUSAL, ENTITY, HYBRID;

    public static MemoryQueryIntent infer(String text) {
        String value = text == null ? "" : text.toLowerCase(java.util.Locale.ROOT);
        if (value.matches(".*(why|because|cause|result|impact|导致|原因|影响|结果).*")) return CAUSAL;
        if (value.matches(".*(when|before|after|during|since|时间|之前|之后|何时|变化).*")) return TEMPORAL;
        if (value.matches(".*(who|which|about|related|关于|哪个|谁|对象).*")) return ENTITY;
        return SEMANTIC;
    }
}

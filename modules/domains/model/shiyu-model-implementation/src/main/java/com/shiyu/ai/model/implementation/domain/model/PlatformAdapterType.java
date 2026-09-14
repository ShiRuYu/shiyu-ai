package com.shiyu.ai.model.implementation.domain.model;

import java.util.Locale;

/**
 * PlatformAdapterType 枚举，定义模型模块可用的业务取值。
 */
public enum PlatformAdapterType {
    OPENAI_COMPATIBLE,
    OLLAMA;

    /**
     * {@code parse} 执行当前类型定义的业务操作。
     *
     * @param value 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static PlatformAdapterType parse(String value) {
        if (value == null || value.isBlank()) {
            return OPENAI_COMPATIBLE;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("不支持的平台接口协议：" + value, ex);
        }
    }
}

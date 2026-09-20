package com.shiyu.ai.model.implementation.domain.model;

import java.util.Locale;

/**
 * 定义 平台 Adapter Type 可用的枚举值及其业务语义。
 */
public enum PlatformAdapterType {
    OPENAI_COMPATIBLE,
    OLLAMA;

    /**
     * 执行 平台 Adapter Type 相关业务数据，并返回处理结果。
     *
     * @param value 用于完成本次业务处理的 value 参数。
     * @return 返回 平台 Adapter Type 相关操作生成的结果数据。
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

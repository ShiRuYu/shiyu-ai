package com.shiyu.ai.model.domain.model;

import java.util.Locale;

/** Supported transport adapters for a model platform. */
public enum PlatformAdapterType {
    OPENAI_COMPATIBLE,
    OLLAMA;

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

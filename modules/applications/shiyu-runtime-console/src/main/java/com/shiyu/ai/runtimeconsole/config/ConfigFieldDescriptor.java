package com.shiyu.ai.runtimeconsole.config;

/** 描述一个可在运行时控制台中编辑的配置字段。 */
public record ConfigFieldDescriptor(
        String key,
        String label,
        String type,
        String applyMode,
        boolean sensitive,
        String source,
        boolean editable,
        String effectiveValue,
        String savedValue,
        boolean configured,
        boolean supported,
        String unsupportedReason) {}

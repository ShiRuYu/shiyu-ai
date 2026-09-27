package com.shiyu.ai.runtimeconsole.config;

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

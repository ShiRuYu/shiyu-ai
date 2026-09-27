package com.shiyu.ai.runtimeconsole.config;

import java.util.List;

public record ConfigApplyResult(
        String status,
        long revision,
        boolean restartRequired,
        String message,
        List<String> issues) {
    public ConfigApplyResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}

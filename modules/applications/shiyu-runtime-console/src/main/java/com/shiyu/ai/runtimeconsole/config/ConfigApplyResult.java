package com.shiyu.ai.runtimeconsole.config;

import java.util.List;

/** 表示运行时配置保存或恢复操作的结果。 */
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

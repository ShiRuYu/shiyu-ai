package com.shiyu.ai.conversation.implementation.domain.chat.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 封装 提示词 Template Version 相关的不可变数据及其字段约束。
 */
public record PromptTemplateVersion(
        String id,
        String templateId,
        int version,
        String status,
        String body,
        Map<String, String> variableSchema,
        List<String> testCases,
        Instant createdAt,
        Instant publishedAt) {
    public PromptTemplateVersion {
        if (version < 1) throw new IllegalArgumentException("version must be positive");
        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status))
            throw new IllegalArgumentException("unsupported prompt status");
        variableSchema = variableSchema == null ? Map.of() : Map.copyOf(variableSchema);
        testCases = testCases == null ? List.of() : List.copyOf(testCases);
    }
}

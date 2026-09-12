package com.shiyu.ai.agent.contract.runtime;

import java.time.Instant;
import java.util.List;

/**
 * {@code ContextItem} 封装智能体模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param sourceType sourceType 属性，表示该记录组件承载的数据。
 * @param sourceId sourceId 属性，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
 * @param content 内容，表示该记录组件承载的数据。
 * @param score 分数，表示该记录组件承载的数据。
 * @param citation citation 属性，表示该记录组件承载的数据。
 * @param relationPath relationPath 属性，表示该记录组件承载的数据。
 * @param accessScope accessScope 属性，表示该记录组件承载的数据。
 * @param tokenCount tokenCount 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record ContextItem(
        String sourceType,
        String sourceId,
        String version,
        String content,
        double score,
        ContextCitation citation,
        List<String> relationPath,
        String accessScope,
        int tokenCount,
        Instant createdAt) {
    public ContextItem(
            String sourceType,
            String sourceId,
            String content,
            double score,
            ContextCitation citation,
            List<String> relationPath,
            String accessScope,
            Instant createdAt) {
        this(
                sourceType,
                sourceId,
                null,
                content,
                score,
                citation,
                relationPath,
                accessScope,
                estimateTokens(content),
                createdAt);
    }

    public ContextItem {
        if (sourceType == null || sourceType.isBlank() || sourceId == null || sourceId.isBlank())
            throw new IllegalArgumentException("context source is required");
        content = content == null ? "" : content;
        relationPath = relationPath == null ? List.of() : List.copyOf(relationPath);
        accessScope = accessScope == null ? "subject" : accessScope;
        tokenCount = tokenCount <= 0 ? estimateTokens(content) : tokenCount;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    private static int estimateTokens(String value) {
        return Math.max(1, value == null ? 0 : value.codePointCount(0, value.length()) / 4);
    }
}

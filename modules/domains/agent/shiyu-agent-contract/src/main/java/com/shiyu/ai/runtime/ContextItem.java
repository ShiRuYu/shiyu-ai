package com.shiyu.ai.runtime;

import java.time.Instant;
import java.util.List;

public record ContextItem(String sourceType, String sourceId, String version, String content, double score,
                          ContextCitation citation, List<String> relationPath, String accessScope,
                          int tokenCount, Instant createdAt) {
    public ContextItem(String sourceType, String sourceId, String content, double score,
                       ContextCitation citation, List<String> relationPath, String accessScope,
                       Instant createdAt) {
        this(sourceType, sourceId, null, content, score, citation, relationPath, accessScope,
                estimateTokens(content), createdAt);
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

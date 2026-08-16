package com.shiyu.ai.runtime;

import java.time.Instant;
import java.util.List;

public record ContextItem(String sourceType, String sourceId, String content, double score,
                          ContextCitation citation, List<String> relationPath, String accessScope,
                          Instant createdAt) {
    public ContextItem {
        if (sourceType == null || sourceType.isBlank() || sourceId == null || sourceId.isBlank())
            throw new IllegalArgumentException("context source is required");
        content = content == null ? "" : content;
        relationPath = relationPath == null ? List.of() : List.copyOf(relationPath);
        accessScope = accessScope == null ? "subject" : accessScope;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}

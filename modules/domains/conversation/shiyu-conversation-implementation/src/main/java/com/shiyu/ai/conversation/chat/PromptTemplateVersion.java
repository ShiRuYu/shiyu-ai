package com.shiyu.ai.conversation.chat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Immutable Prompt Studio revision; publishing creates a new revision instead of mutating history. */
public record PromptTemplateVersion(String id, String templateId, int version, String status,
                                    String body, Map<String, String> variableSchema,
                                    List<String> testCases, Instant createdAt, Instant publishedAt) {
    public PromptTemplateVersion {
        if (version < 1) throw new IllegalArgumentException("version must be positive");
        if (!"DRAFT".equals(status) && !"PUBLISHED".equals(status)) throw new IllegalArgumentException("unsupported prompt status");
        variableSchema = variableSchema == null ? Map.of() : Map.copyOf(variableSchema);
        testCases = testCases == null ? List.of() : List.copyOf(testCases);
    }
}

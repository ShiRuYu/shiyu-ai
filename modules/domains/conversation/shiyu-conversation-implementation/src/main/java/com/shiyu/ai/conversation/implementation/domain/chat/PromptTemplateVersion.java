package com.shiyu.ai.conversation.implementation.domain.chat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 表示提示词模板的版本元数据和发布状态。
 * @param id 标识，表示该记录组件承载的数据。
 * @param templateId templateId 属性，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param body body 属性，表示该记录组件承载的数据。
 * @param variableSchema variableSchema 属性，表示该记录组件承载的数据。
 * @param testCases testCases 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param publishedAt publishedAt 属性，表示该记录组件承载的数据。
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

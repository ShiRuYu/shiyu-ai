package com.shiyu.ai.conversation.implementation.domain.model;

import java.util.Map;

/**
 * 封装 Content Part 相关的不可变数据及其字段约束。
 */
public record ContentPart(
        String type, String text, String mediaUri, String mimeType, Map<String, Object> metadata) {
    public ContentPart {
        if (type == null || type.isBlank())
            throw new IllegalArgumentException("content part type is required");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static ContentPart text(String value) {
        return new ContentPart("text", value == null ? "" : value, null, null, Map.of());
    }
}

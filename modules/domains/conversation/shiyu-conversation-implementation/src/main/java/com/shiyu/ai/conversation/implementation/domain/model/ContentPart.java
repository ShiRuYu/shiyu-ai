package com.shiyu.ai.conversation.implementation.domain.model;

import java.util.Map;

/**
 * 表示消息中的文本、图像或其他结构化内容片段。
 * @param type 类型，表示该记录组件承载的数据。
 * @param text text 属性，表示该记录组件承载的数据。
 * @param mediaUri mediaUri 属性，表示该记录组件承载的数据。
 * @param mimeType mimeType 属性，表示该记录组件承载的数据。
 * @param metadata 元数据，表示该记录组件承载的数据。
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

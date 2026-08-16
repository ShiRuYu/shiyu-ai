package com.shiyu.ai.conversation.domain;

import java.util.Map;

/** A provider-neutral piece of a message. */
public record ContentPart(String type, String text, String mediaUri, String mimeType,
                          Map<String, Object> metadata) {
    public ContentPart {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("content part type is required");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static ContentPart text(String value) {
        return new ContentPart("text", value == null ? "" : value, null, null, Map.of());
    }
}

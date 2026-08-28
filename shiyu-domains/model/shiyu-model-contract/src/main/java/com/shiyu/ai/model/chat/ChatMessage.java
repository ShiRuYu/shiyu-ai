package com.shiyu.ai.model.chat;

import java.util.List;

/** Provider-neutral structured chat message. */
public record ChatMessage(String role, List<ContentPart> content) {
    public ChatMessage {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("message role is required");
        }
        content = content == null ? List.of() : List.copyOf(content);
    }

    public record ContentPart(String type, String text, String uri, String mimeType,
                              String toolCallId, String toolName, String toolArguments, Integer index) {
        public ContentPart(String type, String text, String uri, String mimeType) {
            this(type, text, uri, mimeType, null, null, null, null);
        }
    }

    public static ChatMessage text(String role, String text) {
        return new ChatMessage(role, List.of(new ContentPart("text", text == null ? "" : text, null, null)));
    }
}

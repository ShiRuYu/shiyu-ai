package com.shiyu.ai.conversation.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ConversationMessage(
        String id,
        String conversationId,
        String parentMessageId,
        String sourceMessageId,
        MessageRole role,
        List<ContentPart> contentParts,
        Map<String, Object> toolCall,
        MessageStatus status,
        int sequence,
        String generationId,
        Instant createdAt,
        Instant updatedAt
) {
    public ConversationMessage {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("message id is required");
        if (conversationId == null || conversationId.isBlank()) throw new IllegalArgumentException("conversation id is required");
        if (role == null) throw new IllegalArgumentException("message role is required");
        if (status == null) throw new IllegalArgumentException("message status is required");
        contentParts = contentParts == null ? List.of() : List.copyOf(contentParts);
        toolCall = toolCall == null ? Map.of() : Map.copyOf(toolCall);
    }

    public String textContent() {
        return contentParts.stream().filter(p -> "text".equals(p.type())).map(ContentPart::text)
                .filter(java.util.Objects::nonNull).reduce("", String::concat);
    }
}

package com.shiyu.ai.conversation.implementation.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * {@code ConversationMessage} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param conversationId conversationId 属性，表示该记录组件承载的数据。
 * @param parentMessageId parentMessageId 属性，表示该记录组件承载的数据。
 * @param sourceMessageId sourceMessageId 属性，表示该记录组件承载的数据。
 * @param role 角色，表示该记录组件承载的数据。
 * @param contentParts contentParts 属性，表示该记录组件承载的数据。
 * @param toolCall toolCall 属性，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param sequence sequence 属性，表示该记录组件承载的数据。
 * @param generationId generationId 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
 */
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
        Instant updatedAt) {
    public ConversationMessage {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("message id is required");
        if (conversationId == null || conversationId.isBlank())
            throw new IllegalArgumentException("conversation id is required");
        if (role == null) throw new IllegalArgumentException("message role is required");
        if (status == null) throw new IllegalArgumentException("message status is required");
        contentParts = contentParts == null ? List.of() : List.copyOf(contentParts);
        toolCall = toolCall == null ? Map.of() : Map.copyOf(toolCall);
    }

    public String textContent() {
        return contentParts.stream()
                .filter(p -> "text".equals(p.type()))
                .map(ContentPart::text)
                .filter(java.util.Objects::nonNull)
                .reduce("", String::concat);
    }
}

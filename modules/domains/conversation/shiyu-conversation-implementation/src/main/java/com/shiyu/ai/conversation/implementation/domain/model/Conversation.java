package com.shiyu.ai.conversation.implementation.domain.model;

import java.time.Instant;

/**
 * 封装 会话 相关的不可变数据及其字段约束。
 */
public record Conversation(
        String id,
        long tenantId,
        long ownerUserId,
        String sceneType,
        String title,
        ConversationStatus status,
        String parentConversationId,
        String branchFromMessageId,
        String activeLeafMessageId,
        String rollingSummary,
        String platform,
        String model,
        long version,
        Instant createdAt,
        Instant updatedAt) {
    public Conversation {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("conversation id is required");
        if (sceneType == null || sceneType.isBlank())
            throw new IllegalArgumentException("sceneType is required");
        if (status == null) throw new IllegalArgumentException("status is required");
    }
}

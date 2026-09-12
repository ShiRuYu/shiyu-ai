package com.shiyu.ai.conversation.implementation.domain.model;

import java.time.Instant;

/**
 * {@code Conversation} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param id 标识，表示该记录组件承载的数据。
 * @param tenantId 租户标识，表示该记录组件承载的数据。
 * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
 * @param sceneType sceneType 属性，表示该记录组件承载的数据。
 * @param title 标题，表示该记录组件承载的数据。
 * @param status 状态，表示该记录组件承载的数据。
 * @param parentConversationId parentConversationId 属性，表示该记录组件承载的数据。
 * @param branchFromMessageId branchFromMessageId 属性，表示该记录组件承载的数据。
 * @param activeLeafMessageId activeLeafMessageId 属性，表示该记录组件承载的数据。
 * @param rollingSummary rollingSummary 属性，表示该记录组件承载的数据。
 * @param platform platform 属性，表示该记录组件承载的数据。
 * @param model 模型，表示该记录组件承载的数据。
 * @param version version 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 * @param updatedAt 更新时间，表示该记录组件承载的数据。
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

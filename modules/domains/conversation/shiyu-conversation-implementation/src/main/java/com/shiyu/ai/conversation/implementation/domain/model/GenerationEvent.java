package com.shiyu.ai.conversation.implementation.domain.model;

import java.time.Instant;

/**
 * GenerationEvent 领域事件，描述会话领域相关业务状态变化。
 * @param generationRunId generationRunId 属性，表示该记录组件承载的数据。
 * @param sequence sequence 属性，表示该记录组件承载的数据。
 * @param type 类型，表示该记录组件承载的数据。
 * @param payload payload 属性，表示该记录组件承载的数据。
 * @param createdAt 创建时间，表示该记录组件承载的数据。
 */
public record GenerationEvent(
        String generationRunId,
        int sequence,
        GenerationEventType type,
        String payload,
        Instant createdAt) {
    public GenerationEvent {
        if (generationRunId == null || generationRunId.isBlank())
            throw new IllegalArgumentException("generation run id is required");
        if (sequence < 0) throw new IllegalArgumentException("event sequence must be non-negative");
        if (type == null) throw new IllegalArgumentException("event type is required");
        payload = payload == null ? "" : payload;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}

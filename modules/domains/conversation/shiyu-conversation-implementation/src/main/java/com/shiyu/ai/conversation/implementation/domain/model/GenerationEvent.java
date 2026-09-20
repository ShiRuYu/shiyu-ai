package com.shiyu.ai.conversation.implementation.domain.model;

import java.time.Instant;

/**
 * 封装 生成 相关的不可变数据及其字段约束。
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

package com.shiyu.ai.conversation.domain;

import java.time.Instant;

/** Durable SSE fact. The sequence is the reconnect cursor and is never reused. */
public record GenerationEvent(String generationRunId, int sequence, GenerationEventType type,
                              String payload, Instant createdAt) {
    public GenerationEvent {
        if (generationRunId == null || generationRunId.isBlank()) throw new IllegalArgumentException("generation run id is required");
        if (sequence < 0) throw new IllegalArgumentException("event sequence must be non-negative");
        if (type == null) throw new IllegalArgumentException("event type is required");
        payload = payload == null ? "" : payload;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}

package com.shiyu.ai.agent.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件基类
 */
public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredAt;
    private final String eventType;

    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString().replace("-", "");
        this.occurredAt = Instant.now();
        this.eventType = eventType;
    }

    public String getEventId() { return eventId; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getEventType() { return eventType; }
}

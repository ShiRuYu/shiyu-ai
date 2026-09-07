package com.shiyu.ai.kernel.event;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.time.Instant;
import java.util.Objects;

/** Mandatory ownership and tracing metadata for in-process cross-domain events. */
public record DomainEventEnvelope<E extends DomainEvent>(
        TenantId tenantId,
        UserId userId,
        CorrelationId correlationId,
        Instant occurredAt,
        E event
) {

    public DomainEventEnvelope {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(event, "event must not be null");
    }

    public String eventType() {
        return event.eventType();
    }
}

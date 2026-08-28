package com.shiyu.ai.kernel.event;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventEnvelopeTest {

    private static final DomainEvent EVENT = () -> "agent.execution.completed";

    @Test
    void eventEnvelopeRequiresOwnershipAndTraceability() {
        TenantId tenantId = new TenantId(2);
        UserId userId = new UserId(9);
        CorrelationId correlationId = new CorrelationId("trace-42");
        Instant occurredAt = Instant.parse("2026-08-23T11:00:00Z");

        assertThrows(NullPointerException.class, () ->
                new DomainEventEnvelope<>(null, userId, correlationId, occurredAt, EVENT));
        assertThrows(NullPointerException.class, () ->
                new DomainEventEnvelope<>(tenantId, null, correlationId, occurredAt, EVENT));
        assertThrows(NullPointerException.class, () ->
                new DomainEventEnvelope<>(tenantId, userId, null, occurredAt, EVENT));
        assertThrows(NullPointerException.class, () ->
                new DomainEventEnvelope<>(tenantId, userId, correlationId, null, EVENT));
        assertThrows(NullPointerException.class, () ->
                new DomainEventEnvelope<>(tenantId, userId, correlationId, occurredAt, null));
    }

    @Test
    void envelopeExposesStableEventType() {
        DomainEventEnvelope<DomainEvent> envelope = new DomainEventEnvelope<>(
                new TenantId(2),
                new UserId(9),
                new CorrelationId("trace-42"),
                Instant.parse("2026-08-23T11:00:00Z"),
                EVENT
        );

        assertEquals("agent.execution.completed", envelope.eventType());
    }
}

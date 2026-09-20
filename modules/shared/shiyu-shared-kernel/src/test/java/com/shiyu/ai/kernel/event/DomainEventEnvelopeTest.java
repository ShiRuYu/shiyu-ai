package com.shiyu.ai.kernel.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.time.Instant;

/**
 * 验证 Domain 事件 Envelope 相关功能、边界条件、异常路径和协作行为。
 */
class DomainEventEnvelopeTest {

    private static final DomainEvent EVENT = () -> "agent.execution.completed";

    @Test
    void eventEnvelopeRequiresOwnershipAndTraceability() {
        TenantId tenantId = new TenantId(2);
        UserId userId = new UserId(9);
        CorrelationId correlationId = new CorrelationId("trace-42");
        Instant occurredAt = Instant.parse("2026-08-23T11:00:00Z");

        assertThrows(
                NullPointerException.class,
                () -> new DomainEventEnvelope<>(null, userId, correlationId, occurredAt, EVENT));
        assertThrows(
                NullPointerException.class,
                () -> new DomainEventEnvelope<>(tenantId, null, correlationId, occurredAt, EVENT));
        assertThrows(
                NullPointerException.class,
                () -> new DomainEventEnvelope<>(tenantId, userId, null, occurredAt, EVENT));
        assertThrows(
                NullPointerException.class,
                () -> new DomainEventEnvelope<>(tenantId, userId, correlationId, null, EVENT));
        assertThrows(
                NullPointerException.class,
                () -> new DomainEventEnvelope<>(tenantId, userId, correlationId, occurredAt, null));
    }

    @Test
    void envelopeExposesStableEventType() {
        DomainEventEnvelope<DomainEvent> envelope =
                new DomainEventEnvelope<>(
                        new TenantId(2),
                        new UserId(9),
                        new CorrelationId("trace-42"),
                        Instant.parse("2026-08-23T11:00:00Z"),
                        EVENT);

        assertEquals("agent.execution.completed", envelope.eventType());
    }
}

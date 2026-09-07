package com.shiyu.ai.agent.service;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.agent.event.AuditEvent;
import com.shiyu.ai.agent.event.EventPublisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditServiceTest {
    @Test
    void recordsSuccessAndFailureAsAuditEvents() {
        var publisher = mock(EventPublisher.class);
        var service = new AuditService(publisher);
        service.recordSuccess(new TenantId(1L), 2L, "127.0.0.1", "create", "agent", "a1", java.util.Map.of("x", 1));
        service.recordFailure(new TenantId(1L), 2L, null, "delete", "agent", "a1", null, "denied");
        verify(publisher, org.mockito.Mockito.times(2)).publish(any(AuditEvent.class));
    }

    @Test
    void auditPublishingFailureIsBestEffortAndDoesNotEscape() {
        var publisher = mock(EventPublisher.class);
        doThrow(new IllegalStateException("down")).when(publisher).publish(any());
        var service = new AuditService(publisher);
        assertDoesNotThrow(() -> service.record(new TenantId(1L), 2L, null, "x", "t", "id", null, "FAILED", "err", 9));
    }
}

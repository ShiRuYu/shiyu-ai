package com.shiyu.ai.agent.implementation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.agent.implementation.event.model.AuditEvent;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.port.repository.AuditLogRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * 验证 Audit Service 相关功能、边界条件、异常路径和协作行为。
 */
class AuditServiceTest {
    @AfterEach
    void clearTenantScope() {
        TenantScope.clear();
    }

    @Test
    void recordsSuccessAndFailureAsAuditEvents() {
        var publisher = mock(EventPublisher.class);
        var service = new AuditService(publisher);
        service.recordSuccess(
                new TenantId(1L),
                2L,
                "127.0.0.1",
                "create",
                "agent",
                "a1",
                java.util.Map.of("x", 1));
        service.recordFailure(new TenantId(1L), 2L, null, "delete", "agent", "a1", null, "denied");
        verify(publisher, org.mockito.Mockito.times(2)).publish(any(AuditEvent.class));
    }

    @Test
    void auditPublishingFailureIsBestEffortAndDoesNotEscape() {
        var publisher = mock(EventPublisher.class);
        doThrow(new IllegalStateException("down")).when(publisher).publish(any());
        var service = new AuditService(publisher);
        assertDoesNotThrow(
                () ->
                        service.record(
                                new TenantId(1L),
                                2L,
                                null,
                                "x",
                                "t",
                                "id",
                                null,
                                "FAILED",
                                "err",
                                9));
    }

    @Test
    void requiredSuccessAuditPersistsSynchronouslyAndSkipsAsyncEventPublishing() {
        var publisher = mock(EventPublisher.class);
        var repository = mock(AuditLogRepository.class);
        var tenantId = new TenantId(7L);
        doAnswer(
                        invocation -> {
                            TenantScope.requireMatches(tenantId);
                            return null;
                        })
                .when(repository)
                .insert(eq(tenantId), any(AuditLogBO.class));
        var service = new AuditService(publisher, repository);

        service.recordSuccessRequired(
                tenantId,
                9L,
                null,
                "TOOL_APPROVAL_MANUAL_RECONCILIATION",
                "TOOL_APPROVAL",
                "approval-1",
                java.util.Map.of("runId", "run-1"));

        ArgumentCaptor<AuditLogBO> record = ArgumentCaptor.forClass(AuditLogBO.class);
        verify(repository).insert(eq(tenantId), record.capture());
        assertEquals(7L, record.getValue().getTenantId());
        assertEquals(9L, record.getValue().getUserId());
        assertEquals("TOOL_APPROVAL_MANUAL_RECONCILIATION", record.getValue().getAction());
        assertEquals("TOOL_APPROVAL", record.getValue().getTargetType());
        assertEquals("approval-1", record.getValue().getTargetId());
        assertEquals("{\"runId\":\"run-1\"}", record.getValue().getDetail());
        assertEquals("SUCCESS", record.getValue().getResult());
        assertNotNull(record.getValue().getCreateTime());
        assertEquals(java.util.Optional.empty(), TenantScope.current());
        verifyNoInteractions(publisher);
    }

    @Test
    void requiredSuccessAuditPropagatesPersistenceFailure() {
        var publisher = mock(EventPublisher.class);
        var repository = mock(AuditLogRepository.class);
        var tenantId = new TenantId(7L);
        doThrow(new IllegalStateException("database unavailable"))
                .when(repository)
                .insert(eq(tenantId), any(AuditLogBO.class));
        var service = new AuditService(publisher, repository);

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.recordSuccessRequired(
                                tenantId,
                                9L,
                                null,
                                "TOOL_APPROVAL_MANUAL_RECONCILIATION",
                                "TOOL_APPROVAL",
                                "approval-1",
                                java.util.Map.of("runId", "run-1")));

        verify(repository).insert(eq(tenantId), any(AuditLogBO.class));
        verifyNoInteractions(publisher);
        assertEquals(java.util.Optional.empty(), TenantScope.current());
    }
}

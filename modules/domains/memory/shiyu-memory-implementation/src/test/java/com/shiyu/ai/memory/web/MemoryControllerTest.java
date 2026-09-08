package com.shiyu.ai.memory.web;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.memory.magma.*;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MemoryControllerTest {
    private static final TenantId TENANT = new TenantId(7L);
    private final MagmaMemoryService memory = mock(MagmaMemoryService.class);
    private final MemorySemanticIndex index = mock(MemorySemanticIndex.class);
    private final MemoryController controller = new MemoryController(memory, index);
    private final MemoryEvent event = new MemoryEvent("event-1", TENANT, "magma", "USER", "8", "STUDY", "progress",
            Instant.parse("2026-08-25T00:00:00Z"), "conversation", "source", Map.of(), 0.8, 0.5,
            MemoryEventStatus.ACTIVE, ConfirmationPolicy.AUTO, Instant.now(), Instant.now());

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setCurrentTenantId(7L); context.setHomeTenantId(7L); context.setUserId(8L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void handlesEventQueryAndLifecycleEndpointsWithSubjectIsolation() {
        MemoryController.EventRequest request = new MemoryController.EventRequest();
        request.setNamespace("magma"); request.setSubjectType("USER"); request.setSubjectId("8");
        request.setEventType("STUDY"); request.setContent("progress"); request.setOccurredAt(event.occurredAt());
        request.setSourceType("conversation"); request.setSourceId("source");
        when(memory.ingest(any(IngestMemoryCommand.class))).thenReturn(event);
        assertNotNull(controller.ingest(request));
        MemoryController.QueryRequest query = new MemoryController.QueryRequest();
        query.setNamespace("magma"); query.setSubjectType("USER"); query.setSubjectId("8"); query.setText("progress");
        when(memory.retrieveWithTrace(any(MemoryQuery.class))).thenReturn(mock(MemoryRetrievalResult.class));
        assertNotNull(controller.query(query));
        when(memory.requireAccessibleEvent(TENANT, "event-1")).thenReturn(event);
        assertNotNull(controller.confirm("event-1")); assertNotNull(controller.revoke("event-1"));
        when(memory.relations(TENANT, "event-1", GraphType.TEMPORAL, 50)).thenReturn(List.of());
        assertNotNull(controller.relations("event-1", GraphType.TEMPORAL, 50));
        when(memory.supersede(eq(TENANT), eq("event-1"), any(IngestMemoryCommand.class))).thenReturn(event);
        assertNotNull(controller.supersede("event-1", request));
        assertNotNull(controller.trace("trace-1"));
        controller.rebuild("magma"); verify(index).rebuild(TENANT, "magma");
    }

    @Test
    void rejectsCrossSubjectRequestsBeforeCallingMemory() {
        MemoryController.EventRequest request = new MemoryController.EventRequest();
        request.setNamespace("magma"); request.setSubjectType("USER"); request.setSubjectId("other-user");
        request.setContent("blocked");
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.ingest(request));
        verifyNoInteractions(memory);
    }

    @Test
    void platformAdminCanAccessAnySubjectAndUnscopedTrace() {
        UserContext context = UserContextHolder.getContext();
        context.setCurrentRoleCode("tenant_super");
        UserContextHolder.setContext(context);

        MemoryController.EventRequest request = new MemoryController.EventRequest();
        request.setNamespace("magma");
        request.setSubjectType("GROUP");
        request.setSubjectId("group-1");
        request.setEventType("NOTE");
        request.setContent("admin note");
        request.setOccurredAt(event.occurredAt());
        request.setSourceType("admin");
        request.setSourceId("trace-admin");
        when(memory.ingest(any(IngestMemoryCommand.class))).thenReturn(event);
        assertNotNull(controller.ingest(request));
        assertNotNull(controller.trace("trace-admin"));
        verify(memory).trace(TENANT, "trace-admin", null, null);
    }
}

package com.shiyu.ai.agent.service;

import com.shiyu.ai.agent.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.agent.event.NodeExecutionCompletedEvent;
import com.shiyu.ai.agent.event.NodeExecutionStartedEvent;
import com.shiyu.ai.agent.port.repository.ExecutionTimelineRepository;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TimelineServiceCoverageTest {
    @Test
    void recordsStartAndCompletionAndMapsTimelineRows() {
        ExecutionTimelineRepository repository = mock(ExecutionTimelineRepository.class);
        TimelineService service = new TimelineService(repository);
        TenantId tenant = new TenantId(5);
        service.onNodeStarted(new NodeExecutionStartedEvent(tenant, "exec", "agent", "node", "HTTP", Map.of("q", "x")));
        service.onNodeCompleted(new NodeExecutionCompletedEvent(tenant, "exec", "agent", "node", "HTTP", Map.of("ok", true), "SUCCESS", 42));
        ExecutionTimelineBO row = new ExecutionTimelineBO(); row.setId(1L); row.setTenantId(5L); row.setExecutionId("exec"); row.setAgentId("agent");
        row.setNodeId("node"); row.setNodeType("HTTP"); row.setEventType("NODE_END"); row.setPayload("{}"); row.setDurationMs(42L);
        when(repository.listByExecutionId(tenant, "exec")).thenReturn(List.of(row));
        List<Map<String, Object>> timeline = service.getTimeline(tenant, "exec");
        assertEquals(1, timeline.size());
        assertEquals("NODE_END", timeline.getFirst().get("eventType"));
        verify(repository, times(2)).insert(eq(tenant), any(ExecutionTimelineBO.class));
    }

    @Test
    void telemetryFailuresAreContained() {
        ExecutionTimelineRepository repository = mock(ExecutionTimelineRepository.class);
        doThrow(new IllegalStateException("telemetry down")).when(repository).insert(any(), any());
        TimelineService service = new TimelineService(repository);
        TenantId tenant = new TenantId(5);
        assertDoesNotThrow(() -> service.onNodeStarted(new NodeExecutionStartedEvent(tenant, "exec", "agent", "node", "HTTP", Map.of())));
        assertDoesNotThrow(() -> service.onNodeCompleted(new NodeExecutionCompletedEvent(tenant, "exec", "agent", "node", "HTTP", Map.of(), "FAILED", 1)));
    }
}

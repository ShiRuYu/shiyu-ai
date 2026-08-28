package com.shiyu.ai.agent.event;

import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventPublisherCoverageTest {
    @Test
    void delegatesDomainEventsToSpringPublisher() {
        var spring = mock(ApplicationEventPublisher.class);
        var publisher = new EventPublisher(spring);
        var event = new AuditEvent(new TenantId(1L), 2L, "x", "agent", "a1", null, null, "SUCCESS", null, 0);
        publisher.publish(event);
        verify(spring).publishEvent(event);
    }

    @Test
    void exposesModelCallEventPayload() {
        var event = new ModelCallEvent("openai", "gpt", 10, 4, 25);
        assertEquals("MODEL_CALL", event.getEventType());
        assertEquals("openai", event.getPlatform());
        assertEquals("gpt", event.getModel());
        assertEquals(10, event.getPromptTokens());
        assertEquals(4, event.getCompletionTokens());
        assertEquals(25, event.getLatencyMs());
    }

    @Test
    void exposesExecutionLifecycleEventPayloads() {
        var started = new AgentExecutionStartedEvent("run", "agent", java.util.Map.of("q", "x"));
        assertEquals("AGENT_EXECUTION_STARTED", started.getEventType());
        assertEquals("run", started.getExecutionId());
        assertEquals("agent", started.getAgentId());
        assertEquals("x", started.getInput().get("q"));
        var completed = new AgentExecutionCompletedEvent("run", "agent", java.util.Map.of("ok", true), 12L);
        assertEquals("AGENT_EXECUTION_COMPLETED", completed.getEventType());
        assertEquals("run", completed.getExecutionId());
        assertEquals("agent", completed.getAgentId());
        assertEquals(true, completed.getOutput().get("ok"));
        assertEquals(12L, completed.getDurationMs());
    }
}

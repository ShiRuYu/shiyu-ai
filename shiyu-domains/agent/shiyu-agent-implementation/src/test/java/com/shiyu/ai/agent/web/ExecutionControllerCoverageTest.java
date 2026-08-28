package com.shiyu.ai.agent.web;

import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.runtime.AgentRuntime;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExecutionControllerCoverageTest {
    @Test
    void executesStreamsAndMapsLifecycleResults() {
        AgentRuntime runtime = mock(AgentRuntime.class);
        ExecutionController controller = new ExecutionController(runtime);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        Execution execution = new Execution("agent-1", "v1", Map.of("q", "x"));
        execution.start();
        execution.complete(Map.of("answer", "ok"));
        when(runtime.execute(eq(actor), eq("agent-1"), anyMap())).thenReturn(execution);
        when(runtime.executeStream(eq(actor), eq("agent-1"), anyMap())).thenReturn(Flux.just(Map.of("executionId", "e1", "token", "ok")));
        when(runtime.resume(actor, "e1")).thenReturn(execution);
        when(runtime.getStatus(actor, "e1")).thenReturn(ExecutionStatus.COMPLETED);
        when(runtime.getExecution(actor, "e1")).thenReturn(execution);
        when(runtime.getHistory(actor, "agent-1", 100)).thenReturn(List.of(execution));

        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.execute("agent-1", Map.of("q", "x")).isSuccess());
            assertTrue(controller.executeStream("agent-1", Map.of()).collectList().block().getFirst().isSuccess());
            assertTrue(controller.pause("e1").isSuccess());
            assertTrue(controller.resume("e1").isSuccess());
            assertTrue(controller.cancel("e1").isSuccess());
            assertTrue(controller.getStatus("e1").isSuccess());
            assertTrue(controller.getExecution("e1").isSuccess());
            assertTrue(controller.getHistory("agent-1", 0).isSuccess());
            verify(runtime).pause(actor, "e1");
            verify(runtime).cancel(actor, "e1");
        }
    }

    @Test
    void mapsRuntimeFailuresAndNotFoundStates() {
        AgentRuntime runtime = mock(AgentRuntime.class);
        ExecutionController controller = new ExecutionController(runtime);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        when(runtime.execute(eq(actor), anyString(), anyMap())).thenThrow(new IllegalStateException("boom"));
        when(runtime.executeStream(eq(actor), anyString(), anyMap())).thenReturn(Flux.error(new IllegalStateException("stream")));
        when(runtime.getStatus(actor, "missing")).thenReturn(null);
        when(runtime.getExecution(actor, "missing")).thenReturn(null);
        when(runtime.resume(actor, "e")).thenThrow(new IllegalArgumentException("bad"));
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertFalse(controller.execute("a", null).isSuccess());
            assertFalse(controller.executeStream("a", null).collectList().block().getFirst().isSuccess());
            assertFalse(controller.getStatus("missing").isSuccess());
            assertFalse(controller.getExecution("missing").isSuccess());
            assertFalse(controller.resume("e").isSuccess());
            doThrow(new IllegalStateException("pause")).when(runtime).pause(actor, "e");
            doThrow(new IllegalStateException("cancel")).when(runtime).cancel(actor, "e");
            assertFalse(controller.pause("e").isSuccess());
            assertFalse(controller.cancel("e").isSuccess());
        }
    }
}

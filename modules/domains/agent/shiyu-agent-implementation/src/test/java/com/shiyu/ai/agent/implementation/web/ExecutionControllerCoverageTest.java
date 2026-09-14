package com.shiyu.ai.agent.implementation.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

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
        when(runtime.executeStream(eq(actor), eq("agent-1"), anyMap()))
                .thenReturn(Flux.just(Map.of("executionId", "e1", "token", "ok")));
        when(runtime.resume(actor, "e1")).thenReturn(execution);
        when(runtime.getStatus(actor, "e1")).thenReturn(ExecutionStatus.COMPLETED);
        when(runtime.getExecution(actor, "e1")).thenReturn(execution);
        when(runtime.getHistory(actor, "agent-1", 100)).thenReturn(List.of(execution));

        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.execute("agent-1", Map.of("q", "x")).isSuccess());
            assertTrue(
                    controller
                            .executeStream("agent-1", Map.of())
                            .collectList()
                            .block()
                            .getFirst()
                            .isSuccess());
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
        when(runtime.execute(eq(actor), anyString(), anyMap()))
                .thenThrow(new IllegalStateException("boom"));
        when(runtime.executeStream(eq(actor), anyString(), anyMap()))
                .thenReturn(Flux.error(new IllegalStateException("stream")));
        when(runtime.getStatus(actor, "missing")).thenReturn(null);
        when(runtime.getExecution(actor, "missing")).thenReturn(null);
        when(runtime.resume(actor, "e")).thenThrow(new IllegalArgumentException("bad"));
        Execution failed = new Execution("agent-1", "v1", Map.of());
        failed.start();
        failed.fail("jdbc password=secret");
        when(runtime.getExecution(actor, "failed")).thenReturn(failed);
        when(runtime.getHistory(actor, "agent-1", 1)).thenReturn(List.of(failed));
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            var executeFailure = controller.execute("a", null);
            var streamFailure =
                    controller.executeStream("a", null).collectList().block().getFirst();
            assertFalse(executeFailure.isSuccess());
            assertFalse(streamFailure.isSuccess());
            assertEquals("执行失败，请稍后重试", executeFailure.getMessage());
            assertEquals("流式执行失败，请稍后重试", streamFailure.getMessage());
            assertFalse(executeFailure.getMessage().contains("boom"));
            assertFalse(streamFailure.getMessage().contains("stream"));
            assertFalse(controller.getStatus("missing").isSuccess());
            assertFalse(controller.getExecution("missing").isSuccess());
            var failedDetail = controller.getExecution("failed");
            assertEquals("执行失败，请稍后重试", failedDetail.getData().get("errorMessage"));
            assertFalse(
                    String.valueOf(failedDetail.getData().get("errorMessage")).contains("secret"));
            var failedHistory = controller.getHistory("agent-1", 1);
            assertEquals("执行失败，请稍后重试", failedHistory.getData().getFirst().get("errorMessage"));
            var resumeFailure = controller.resume("e");
            assertFalse(resumeFailure.isSuccess());
            assertEquals("恢复执行失败，请稍后重试", resumeFailure.getMessage());
            doThrow(new IllegalStateException("pause")).when(runtime).pause(actor, "e");
            doThrow(new IllegalStateException("cancel")).when(runtime).cancel(actor, "e");
            var pauseFailure = controller.pause("e");
            var cancelFailure = controller.cancel("e");
            assertFalse(pauseFailure.isSuccess());
            assertFalse(cancelFailure.isSuccess());
            assertEquals("暂停失败，请稍后重试", pauseFailure.getMessage());
            assertEquals("取消失败，请稍后重试", cancelFailure.getMessage());
        }
    }
}

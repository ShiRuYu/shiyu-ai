package com.shiyu.ai.agent.implementation.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunContext;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunSource;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.mockito.ArgumentCaptor;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

/** 验证运行记录事件写入失败后不会留下未结束的运行记录。 */
class AgentRuntimeEventBridgeTest {
    @Test
    void executionRunIsVisibleOnlyThroughTheRuntimeOwnershipCheck() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntimeEventBridge bridge = new AgentRuntimeEventBridge(runtime);
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        AiRun owned = new AiRun("run-4", new TenantId(9), new UserId(7),
                null, null, AiRunSource.AGENT, "agent-1", null, null,
                null, null, "execution-4", null, "hash", AiRunStatus.RUNNING,
                0, 0, true, null, Instant.now(), null, null, 1);
        when(runtime.requireExecutionRun("execution-4", new TenantId(9), 7L))
                .thenReturn(owned);
        when(runtime.requireExecutionRun("foreign", new TenantId(9), 7L))
                .thenThrow(new IllegalArgumentException("not owned"));

        assertSame(owned, bridge.executionRun(actor, "execution-4"));
        assertNull(bridge.executionRun(actor, "foreign"));
        assertNull(new AgentRuntimeEventBridge(null).executionRun(actor, "execution-4"));
    }

    @Test
    void startFailureStillRejectsAppAdmissionWhenCleanupAlsoFails() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntimeEventBridge bridge = new AgentRuntimeEventBridge(runtime);
        AiRun admitted = new AiRun("run-5", new TenantId(9), new UserId(7),
                "app-1", null, AiRunSource.AGENT, "agent-1", null, null,
                null, null, "execution-5", null, "hash", AiRunStatus.RUNNING,
                0, 0, true, null, Instant.now(), null, null, 1);
        when(runtime.startRun(any(AiRunContext.class), eq(AiRunSource.AGENT),
                eq("agent-1"), isNull(), anyString())).thenReturn(admitted);
        doThrow(new IllegalStateException("event store down")).when(runtime)
                .append(admitted, AiRunEventType.MODEL_STARTED, "{}", true);
        doThrow(new IllegalStateException("runtime store down")).when(runtime)
                .finish("run-5", new TenantId(9), 7L, AiRunStatus.FAILED,
                        "RUN_EVENT_WRITE_FAILED");

        Execution execution = new Execution("agent-1", "v1", Map.of());
        assertThrows(IllegalStateException.class, () -> bridge.start(execution,
                Map.of("tenantId", 9L, "userId", 7L, "__appId", "app-1")));
        verify(runtime).finish("run-5", new TenantId(9), 7L,
                AiRunStatus.FAILED, "RUN_EVENT_WRITE_FAILED");
    }

    @Test
    void admittedAppRunUsesExecutionOwnerAndKeepsConversationLink() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AiRun admitted = new AiRun("run-2", new TenantId(9), new UserId(7),
                "app-1", "version-1", AiRunSource.AGENT, "agent-1", null, null,
                "conversation-1", null, "execution-2", null, "hash", AiRunStatus.RUNNING,
                0, 0, true, null, Instant.now(), null, null, 1);
        when(runtime.startRun(any(AiRunContext.class), eq(AiRunSource.AGENT),
                eq("agent-1"), isNull(), anyString())).thenReturn(admitted);
        AgentRuntimeEventBridge bridge = new AgentRuntimeEventBridge(runtime);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.setUserId(7L);
        Map<String, Object> input = Map.of("tenantId", "9", "userId", 8L,
                "__appId", "app-1", "__appVersionId", "version-1",
                "__conversationId", "conversation-1");

        assertSame(admitted, bridge.start(execution, input));
        ArgumentCaptor<AiRunContext> captured = ArgumentCaptor.forClass(AiRunContext.class);
        verify(runtime).startRun(captured.capture(), eq(AiRunSource.AGENT),
                eq("agent-1"), isNull(), anyString());
        assertEquals(new TenantId(9), captured.getValue().tenantId());
        assertEquals(7L, captured.getValue().ownerUserId());
        assertEquals("app-1", captured.getValue().appId());
        assertEquals("version-1", captured.getValue().appVersionId());
        assertEquals("conversation-1", captured.getValue().conversationId());
        assertEquals(execution.getExecutionId(), captured.getValue().executionId());
        verify(runtime).append(admitted, AiRunEventType.MODEL_STARTED, "{}", true);

        execution.start();
        execution.complete(Map.of("answer", "OK"));
        bridge.finish(admitted, execution);
        verify(runtime).finish("run-2", new TenantId(9), 7L,
                AiRunStatus.COMPLETED, null);
    }

    @Test
    void rejectsUnownedRunsAndMapsFailureAndCancellationSeparately() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AgentRuntimeEventBridge bridge = new AgentRuntimeEventBridge(runtime);
        Execution invalid = new Execution("agent-1", "v1", Map.of());
        assertNull(bridge.start(invalid, null));
        assertNull(bridge.start(invalid, Map.of("tenantId", "bad", "userId", 7L)));
        assertNull(bridge.start(invalid, Map.of("tenantId", 9L, "userId", 0L)));
        verify(runtime, never()).startRun(any(), any(), any(), any(), any());

        AiRun admitted = new AiRun("run-3", new TenantId(9), new UserId(7),
                null, null, AiRunSource.AGENT, "agent-1", null, null,
                null, null, "execution-3", null, "hash", AiRunStatus.RUNNING,
                0, 0, true, null, Instant.now(), null, null, 1);
        Execution failed = new Execution("agent-1", "v1", Map.of());
        failed.start();
        failed.fail("node failed");
        bridge.finish(admitted, failed);
        verify(runtime).finish("run-3", new TenantId(9), 7L,
                AiRunStatus.FAILED, "AGENT_EXECUTION_FAILED");

        Execution cancelled = new Execution("agent-1", "v1", Map.of());
        cancelled.start();
        cancelled.cancel();
        bridge.finish(admitted, cancelled);
        verify(runtime).finish("run-3", new TenantId(9), 7L,
                AiRunStatus.CANCELLED, null);
    }

    @Test
    void closesAdmittedRunWhenModelStartedEventCannotBeWritten() {
        AiRuntimeService runtime = mock(AiRuntimeService.class);
        AiRun admitted = new AiRun("run-1", new TenantId(9), new UserId(7),
                null, null, AiRunSource.AGENT, "agent-1", null, null, null,
                null, "execution-1", null, "hash", AiRunStatus.RUNNING,
                0, 0, true, null, Instant.now(), null, null, 1);
        when(runtime.startRun(any(AiRunContext.class), eq(AiRunSource.AGENT),
                eq("agent-1"), isNull(), anyString())).thenReturn(admitted);
        doThrow(new IllegalStateException("event store down")).when(runtime)
                .append(admitted, AiRunEventType.MODEL_STARTED, "{}", true);
        AgentRuntimeEventBridge bridge = new AgentRuntimeEventBridge(runtime);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        Map<String, Object> input = Map.of("tenantId", 9L, "userId", 7L);

        assertNull(bridge.start(execution, input));

        verify(runtime).finish("run-1", new TenantId(9), 7L,
                AiRunStatus.FAILED, "RUN_EVENT_WRITE_FAILED");
        assertThrows(IllegalStateException.class,
                () -> bridge.start(execution, Map.of("tenantId", 9L, "userId", 7L,
                        "__appId", "app-1")));
    }
}

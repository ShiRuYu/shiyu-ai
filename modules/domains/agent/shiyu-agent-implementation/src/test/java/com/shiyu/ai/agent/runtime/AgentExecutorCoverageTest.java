package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.checkpoint.Checkpoint;
import com.shiyu.ai.agent.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentExecutorCoverageTest {
    private static final TenantId TENANT = new TenantId(9L);
    private static final AgentDefinition DEFINITION = AgentDefinition.builder().agentId("agent-1").build();

    @Test
    void executesPendingExecutionAndPersistsFinalCheckpoint() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = mock(Graph.class);
        Map<String, Object> output = Map.of("answer", "ok");
        when(graph.execute(any())).thenReturn(output);
        Execution execution = new Execution("agent-1", "v1", Map.of("input", "value"));

        Execution result = new AgentExecutor(checkpoints).executeAgent(
                TENANT, DEFINITION, version(graph), execution.getInput(), execution);

        assertSame(execution, result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals(output, result.getOutput());
        assertEquals(1, result.getNodeExecutions().size());
        verify(checkpoints).createCheckpoint(TENANT, execution.getExecutionId(), "graph_final", output);
    }

    @Test
    void returnsCancelledExecutionBeforeGraphInvocation() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = mock(Graph.class);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.cancel();

        Execution result = new AgentExecutor(checkpoints).executeAgent(
                TENANT, DEFINITION, version(graph), Map.of(), execution);

        assertEquals(ExecutionStatus.CANCELLED, result.getStatus());
        verify(graph, never()).execute(any());
        verify(checkpoints, never()).createCheckpoint(any(), anyString(), anyString(), any());
    }

    @Test
    void failsWhenGraphIsMissingOrCheckpointWriteFails() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Execution missingGraph = new Execution("agent-1", "v1", Map.of());
        Execution failed = new AgentExecutor(checkpoints).executeAgent(
                TENANT, DEFINITION, version(null), Map.of(), missingGraph);
        assertEquals(ExecutionStatus.FAILED, failed.getStatus());
        assertEquals("执行异常: Agent 版本 graph 为空", failed.getErrorMessage());

        Graph graph = mock(Graph.class);
        when(graph.execute(any())).thenReturn(Map.of("ok", true));
        doThrow(new IllegalStateException("storage unavailable"))
                .when(checkpoints).createCheckpoint(any(), anyString(), anyString(), any());
        Execution checkpointFailure = new Execution("agent-1", "v1", Map.of());
        Execution result = new AgentExecutor(checkpoints).executeAgent(
                TENANT, DEFINITION, version(graph), Map.of(), checkpointFailure);
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertEquals("执行异常: storage unavailable", result.getErrorMessage());
    }

    @Test
    void cancellationAfterGraphExecutionSkipsCheckpointAndCompletion() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = mock(Graph.class);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        doAnswer(invocation -> {
            execution.cancel();
            return Map.of("ignored", true);
        }).when(graph).execute(any());

        Execution result = new AgentExecutor(checkpoints).executeAgent(
                TENANT, DEFINITION, version(graph), Map.of(), execution);

        assertEquals(ExecutionStatus.CANCELLED, result.getStatus());
        verify(checkpoints, never()).createCheckpoint(any(), anyString(), anyString(), any());
    }

    @Test
    void resumesFromCheckpointAndHandlesResumeFailure() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = mock(Graph.class);
        Map<String, Object> state = Map.of("step", 1);
        Map<String, Object> output = Map.of("step", 2);
        when(graph.execute(any())).thenReturn(output);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.start();
        Execution result = new AgentExecutor(checkpoints).resumeFromCheckpoint(
                TENANT, execution, DEFINITION, version(graph),
                new Checkpoint(TENANT, execution.getExecutionId(), "node", state));
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals(output, result.getOutput());
        assertEquals(1, result.getNodeExecutions().size());

        Graph broken = mock(Graph.class);
        when(broken.execute(any())).thenThrow(new IllegalStateException("resume failed"));
        Execution failed = new Execution("agent-1", "v1", Map.of());
        failed.start();
        Execution failedResult = new AgentExecutor(checkpoints).resumeFromCheckpoint(
                TENANT, failed, DEFINITION, version(broken),
                new Checkpoint(TENANT, failed.getExecutionId(), "node", state));
        assertEquals(ExecutionStatus.FAILED, failedResult.getStatus());
        assertEquals("恢复执行异常: resume failed", failedResult.getErrorMessage());
    }

    @Test
    void retryAndTimeoutPathReturnsGraphResult() throws Exception {
        Graph graph = mock(Graph.class);
        Map<String, Object> output = Map.of("ok", true);
        when(graph.execute(any())).thenReturn(output);
        Map<String, Object> result = new AgentExecutor(mock(CheckpointManager.class))
                .executeWithRetryAndTimeout(DEFINITION, version(graph), Map.of("input", "x"));
        assertEquals(output, result);
    }

    private static AgentVersion version(Graph graph) {
        return AgentVersion.builder().versionNumber("v1").graph(graph).build();
    }
}

package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.service.AgentExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.implementation.checkpoint.Checkpoint;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.graph.Graph;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证 智能体 Executor Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class AgentExecutorCoverageTest {
    private static final TenantId TENANT = new TenantId(9L);
    private static final AgentDefinition DEFINITION =
            AgentDefinition.builder().agentId("agent-1").build();

    @Test
    void executesPendingExecutionAndPersistsFinalCheckpoint() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = graph(() -> {}, "answer", "ok");
        Execution execution = new Execution("agent-1", "v1", Map.of("input", "value"));
        when(checkpoints.createCheckpoint(eq(TENANT), eq(execution.getExecutionId()),
                eq("graph_final"), any())).thenAnswer(invocation ->
                new com.shiyu.ai.agent.implementation.checkpoint.Checkpoint(TENANT,
                        execution.getExecutionId(), "graph_final", invocation.getArgument(3)));

        Execution result =
                new AgentExecutor(checkpoints)
                        .executeAgent(
                                TENANT,
                                DEFINITION,
                                version(graph),
                                execution.getInput(),
                                execution);

        assertSame(execution, result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals("ok", result.getOutput().get("answer"));
        assertEquals(1, result.getNodeExecutions().size());
        verify(checkpoints)
                .createCheckpoint(TENANT, execution.getExecutionId(), "graph_final",
                        result.getOutput());
    }

    @Test
    void returnsCancelledExecutionBeforeGraphInvocation() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = mock(Graph.class);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.cancel();

        Execution result =
                new AgentExecutor(checkpoints)
                        .executeAgent(TENANT, DEFINITION, version(graph), Map.of(), execution);

        assertEquals(ExecutionStatus.CANCELLED, result.getStatus());
        verify(graph, never()).execute(any());
        verify(checkpoints, never()).createCheckpoint(any(), anyString(), anyString(), any());
    }

    @Test
    void failsWhenGraphIsMissingOrCheckpointWriteFails() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Execution missingGraph = new Execution("agent-1", "v1", Map.of());
        Execution failed =
                new AgentExecutor(checkpoints)
                        .executeAgent(TENANT, DEFINITION, version(null), Map.of(), missingGraph);
        assertEquals(ExecutionStatus.FAILED, failed.getStatus());
        assertEquals("执行异常: Agent 版本 graph 为空", failed.getErrorMessage());

        Graph graph = graph(() -> {}, "ok", true);
        doThrow(new IllegalStateException("storage unavailable"))
                .when(checkpoints)
                .createCheckpoint(any(), anyString(), anyString(), any());
        Execution checkpointFailure = new Execution("agent-1", "v1", Map.of());
        Execution result =
                new AgentExecutor(checkpoints)
                        .executeAgent(
                                TENANT, DEFINITION, version(graph), Map.of(), checkpointFailure);
        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertEquals("执行异常: storage unavailable", result.getErrorMessage());
    }

    @Test
    void cancellationAfterGraphExecutionSkipsCheckpointAndCompletion() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        Graph graph = graph(execution::cancel, "ignored", true);

        Execution result =
                new AgentExecutor(checkpoints)
                        .executeAgent(TENANT, DEFINITION, version(graph), Map.of(), execution);

        assertEquals(ExecutionStatus.CANCELLED, result.getStatus());
        verify(checkpoints, never()).createCheckpoint(any(), anyString(), anyString(), any());
    }

    @Test
    void intermediateCheckpointNeverReplaysCompletedSideEffect() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = mock(Graph.class);
        AtomicInteger sideEffects = new AtomicInteger(1); // The completed node already called a tool.
        doAnswer(invocation -> {
            sideEffects.incrementAndGet();
            return Map.of("step", 2);
        }).when(graph).execute(any());
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.start();
        Execution result =
                new AgentExecutor(checkpoints)
                        .resumeFromCheckpoint(
                                new ActorContext(TENANT, new UserId(7L), false),
                                execution,
                                DEFINITION,
                                version(graph),
                                new Checkpoint(TENANT, execution.getExecutionId(), "tool_done",
                                        Map.of("step", 1)));

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("检查点缺少下一节点"));
        assertEquals(1, sideEffects.get());
        verify(graph, never()).execute(any());
    }

    @Test
    void finalCheckpointCompletesFromSavedOutputWithoutReexecutingGraph() throws Exception {
        CheckpointManager checkpoints = mock(CheckpointManager.class);
        Graph graph = mock(Graph.class);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.start();
        Execution result =
                new AgentExecutor(checkpoints)
                        .resumeFromCheckpoint(
                                new ActorContext(TENANT, new UserId(7L), false),
                                execution,
                                DEFINITION,
                                version(graph),
                                new Checkpoint(TENANT, execution.getExecutionId(), "graph_final",
                                        Map.of("answer", "saved", "__knowledgeAccessContext", "forged")));

        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals(Map.of("answer", "saved"), result.getOutput());
        assertFalse(result.getOutput().containsKey("__knowledgeAccessContext"));
        verify(graph, never()).execute(any());
    }

    @Test
    void rejectsCheckpointFromAnotherExecutionOrTenantAndDoesNotReviveCancellation()
            throws Exception {
        AgentExecutor executor = new AgentExecutor(mock(CheckpointManager.class));
        Graph graph = mock(Graph.class);
        ActorContext actor = new ActorContext(TENANT, new UserId(7L), false);
        Execution wrongExecution = new Execution("agent-1", "v1", Map.of());
        wrongExecution.start();
        Execution mismatch = executor.resumeFromCheckpoint(actor, wrongExecution,
                DEFINITION, version(graph), new Checkpoint(TENANT, "another-run",
                        "graph_final", Map.of("answer", "foreign")));
        assertEquals(ExecutionStatus.FAILED, mismatch.getStatus());
        assertTrue(mismatch.getErrorMessage().contains("检查点与当前执行不匹配"));

        Execution cancelled = new Execution("agent-1", "v1", Map.of());
        cancelled.start();
        cancelled.cancel();
        Execution unchanged = executor.resumeFromCheckpoint(actor, cancelled,
                DEFINITION, version(graph), new Checkpoint(TENANT, cancelled.getExecutionId(),
                        "graph_final", Map.of("answer", "saved")));
        assertEquals(ExecutionStatus.CANCELLED, unchanged.getStatus());
        verify(graph, never()).execute(any());
    }

    @Test
    void retryAndTimeoutPathReturnsGraphResult() throws Exception {
        Graph graph = mock(Graph.class);
        Map<String, Object> output = Map.of("ok", true);
        when(graph.execute(any())).thenReturn(output);
        Map<String, Object> result =
                new AgentExecutor(mock(CheckpointManager.class))
                        .executeWithRetryAndTimeout(
                                DEFINITION, version(graph), Map.of("input", "x"));
        assertEquals(output, result);
    }

    private static AgentVersion version(Graph graph) {
        return AgentVersion.builder().versionNumber("v1").graph(graph).build();
    }

    private static Graph graph(Runnable action, String outputKey, Object outputValue) {
        BaseNode node = new BaseNode(NodeConfig.builder().nodeId("node")
                .nodeName("node").nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                action.run();
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData(outputKey, outputValue);
                return output;
            }
        };
        BaseNode finish = new BaseNode(NodeConfig.builder().nodeId("finish")
                .nodeName("finish").nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                return output;
            }
        };
        return Graph.builder().name("executor-test").startNode("node")
                .endNode("finish").nodes(Map.of("node", node, "finish", finish))
                .edges(Map.of("node", List.of("finish")))
                .conditionalEdges(Map.of()).build();
    }
}

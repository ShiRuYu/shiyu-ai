package com.shiyu.ai.agent.implementation.checkpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.implementation.domain.model.AgentCheckpointBO;
import com.shiyu.ai.agent.implementation.graph.Graph;
import com.shiyu.ai.agent.implementation.graph.StateGraphBuilder;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallNode;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.tooling.contract.api.ToolService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.contract.model.KnowledgeResponse;
import com.shiyu.ai.knowledge.contract.model.KnowledgeDocumentDTO;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/** 验证数据库检查点在重新编译图后仍能从下一节点继续。 */
class AgentGraphCheckpointSaverTest {
    private static final TenantId TENANT = new TenantId(9L);
    private static final ActorContext ACTOR =
            new ActorContext(TENANT, new UserId(7L), false);

    @Test
    void checkpointSaverRejectsIncompleteExecutionIdentityBeforeWriting() {
        CheckpointManager manager = mock(CheckpointManager.class);
        assertThrows(IllegalArgumentException.class,
                () -> new AgentGraphCheckpointSaver(null, "run-1", manager));
        assertThrows(IllegalArgumentException.class,
                () -> new AgentGraphCheckpointSaver(TENANT, null, manager));
        assertThrows(IllegalArgumentException.class,
                () -> new AgentGraphCheckpointSaver(TENANT, " ", manager));
        assertThrows(IllegalArgumentException.class,
                () -> new AgentGraphCheckpointSaver(TENANT, "run-1", null));
        assertThrows(IllegalArgumentException.class,
                () -> new AgentGraphCheckpointSaver(TENANT, "run-1", manager, null));

        AgentGraphCheckpointSaver saver = new AgentGraphCheckpointSaver(TENANT, "run-1", manager);
        RunnableConfig owned = RunnableConfig.builder().threadId("run-1").build();
        assertThrows(IllegalArgumentException.class,
                () -> saver.markNodeStarted(owned, null, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> saver.markNodeStarted(owned, " ", Map.of()));
        verifyNoInteractions(manager);
    }

    @Test
    void foreignExecutionCannotWriteStartedOrApprovalCheckpoint() {
        CheckpointManager manager = mock(CheckpointManager.class);
        AgentGraphCheckpointSaver saver = new AgentGraphCheckpointSaver(TENANT, "owned", manager);
        RunnableConfig foreign = RunnableConfig.builder().threadId("foreign").build();

        assertThrows(IllegalStateException.class,
                () -> saver.markNodeStarted(foreign, "tool", Map.of()));
        assertThrows(IllegalStateException.class,
                () -> saver.markApprovalPending(foreign, "tool", Map.of(), "approval-1"));

        verifyNoInteractions(manager);
    }

    @Test
    void incompleteApprovalCheckpointCannotBePersisted() {
        CheckpointManager manager = mock(CheckpointManager.class);
        AgentGraphCheckpointSaver saver = new AgentGraphCheckpointSaver(TENANT, "owned", manager);
        RunnableConfig owned = RunnableConfig.builder().threadId("owned").build();

        assertThrows(IllegalArgumentException.class,
                () -> saver.markApprovalPending(owned, null, Map.of(), "approval-1"));
        assertThrows(IllegalArgumentException.class,
                () -> saver.markApprovalPending(owned, " ", Map.of(), "approval-1"));
        assertThrows(IllegalArgumentException.class,
                () -> saver.markApprovalPending(owned, "tool", Map.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> saver.markApprovalPending(owned, "tool", Map.of(), " "));
        assertThrows(IllegalArgumentException.class,
                () -> saver.put(owned, null));
        assertThrows(IllegalStateException.class,
                () -> saver.put(null, null));
        var missingId = mock(org.bsc.langgraph4j.checkpoint.Checkpoint.class);
        when(missingId.getNextNodeId()).thenReturn("finish");
        assertThrows(IllegalArgumentException.class,
                () -> saver.put(owned, missingId));
        var missingNextNode = mock(org.bsc.langgraph4j.checkpoint.Checkpoint.class);
        when(missingNextNode.getId()).thenReturn("checkpoint-1");
        assertThrows(IllegalArgumentException.class,
                () -> saver.put(owned, missingNextNode));

        verifyNoInteractions(manager);
    }

    @Test
    void publishesCheckpointIdentityOnlyAfterTheCheckpointIsSaved() {
        CheckpointManager manager = mock(CheckpointManager.class);
        AtomicReference<Checkpoint> persisted = new AtomicReference<>();
        AtomicReference<String> associated = new AtomicReference<>();
        doAnswer(invocation -> {
            persisted.set(invocation.getArgument(0));
            return null;
        }).when(manager).saveCheckpoint(any(Checkpoint.class));
        AgentGraphCheckpointSaver saver = new AgentGraphCheckpointSaver(
                TENANT, "owned", manager, id -> {
                    assertEquals(id, persisted.get().getCheckpointId());
                    associated.set(id);
                });
        RunnableConfig config = RunnableConfig.builder().threadId("owned").build();

        saver.markNodeStarted(config, "tool", Map.of());
        assertEquals(persisted.get().getCheckpointId(), associated.get());
        saver.put(config, org.bsc.langgraph4j.checkpoint.Checkpoint.builder()
                .id("result-1").nodeId("tool").nextNodeId("finish")
                .state(Map.of("answer", "ok")).build());
        assertEquals("result-1", associated.get());
    }

    @Test
    void checkpointLookupRejectsForeignIdentityAndUnsafeHistory() {
        CheckpointManager manager = mock(CheckpointManager.class);
        AgentGraphCheckpointSaver saver = new AgentGraphCheckpointSaver(TENANT, "owned", manager);
        RunnableConfig owned = RunnableConfig.builder().threadId("owned").build();
        RunnableConfig foreign = RunnableConfig.builder().threadId("foreign").build();
        Checkpoint otherExecution = new Checkpoint(TENANT, "foreign", "other", "first",
                "next", Map.of());
        Checkpoint otherTenant = new Checkpoint(new TenantId(10), "owned", "other-tenant",
                "first", "next", Map.of());
        Checkpoint legacy = new Checkpoint(TENANT, "owned", "legacy", "first", null, Map.of());
        Checkpoint safe = new Checkpoint(TENANT, "owned", "safe", "first", "next", Map.of());
        Checkpoint started = new Checkpoint(TENANT, "owned", "started",
                "__agent_node_started__", "next", Map.of());
        Checkpoint nonresumable = new Checkpoint(TENANT, "owned", "nonresumable",
                "__agent_nonresumable__", "next", Map.of());

        assertThrows(IllegalStateException.class, () -> saver.get(foreign));
        assertThrows(IllegalStateException.class, () -> saver.list(foreign));
        when(manager.loadLatestCheckpoint(TENANT, "owned"))
                .thenReturn(otherExecution, otherTenant, legacy, safe, started, nonresumable);
        assertTrue(saver.get(owned).isEmpty());
        assertTrue(saver.get(owned).isEmpty());
        assertTrue(saver.get(owned).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> saver.get(owned.withCheckPointId("older")));
        assertThrows(IllegalStateException.class, () -> saver.get(owned));
        assertThrows(IllegalStateException.class, () -> saver.get(owned));

        when(manager.listCheckpoints(TENANT, "owned"))
                .thenReturn(List.of(legacy, safe, started, nonresumable));
        assertEquals(List.of("safe"), saver.list(owned).stream().map(
                org.bsc.langgraph4j.checkpoint.Checkpoint::getId).toList());
    }

    @Test
    void nonSerializableToolValueCanPassThroughCompiledGraph() throws Exception {
        /** 模拟不实现序列化接口的工具返回值。 */
        record ToolPayload(String answer) {}
        ToolService toolService = mock(ToolService.class);
        when(toolService.execute(eq("search"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(
                        true, new ToolPayload("found"), null));
        ToolCallNode tool = ToolCallNode.builder().toolService(toolService)
                .config(ToolCallConfig.builder().toolName("search").build()).build();
        BaseNode consume = new BaseNode(NodeConfig.builder().nodeId("consume")
                .nodeName("consume").nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                assertEquals(Map.of("answer", "found"), input.getParameter("toolResult"));
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("result", "done");
                return output;
            }
        };
        Graph graph = Graph.builder().name("tool-value")
                .startNode("tool").endNode("consume")
                .nodes(Map.of("tool", tool, "consume", consume))
                .edges(Map.of("tool", List.of("consume")))
                .conditionalEdges(Map.of()).build();

        var result = StateGraphBuilder.fromGraph(graph).build().invoke(
                Map.of(), RunnableConfig.builder().threadId("tool-value").build());

        assertEquals("done", result.orElseThrow().data().get("result"));
    }

    @Test
    void restartedGraphResumesAfterPersistedSideEffectWithoutReplayingIt() throws Exception {
        String executionId = "execution-after-restart";
        AgentCheckpointRepository repository = mock(AgentCheckpointRepository.class);
        List<AgentCheckpointBO> stored = new ArrayList<>();
        doAnswer(invocation -> {
            AgentCheckpointBO checkpoint = invocation.getArgument(1);
            checkpoint.setTenantId(TENANT.value());
            stored.add(checkpoint);
            return null;
        }).when(repository).insert(eq(TENANT), any(AgentCheckpointBO.class));
        when(repository.selectLatestByExecutionId(TENANT, executionId))
                .thenAnswer(invocation -> stored.isEmpty() ? null
                        : stored.get(stored.size() - 1));

        AtomicInteger sideEffects = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();
        Graph graph = graph(sideEffects, completed);
        RunnableConfig config = RunnableConfig.builder().threadId(executionId).build();
        CheckpointManager firstManager = new CheckpointManager(new DbCheckpointStore(repository));
        var first = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, executionId, firstManager))
                .interruptAfter("side_effect").build());

        first.invoke(NodeActorContext.bind(ACTOR, Map.of("studentId", 42L)), config);

        assertEquals(1, sideEffects.get());
        assertEquals(0, completed.get());
        assertTrue(stored.stream().anyMatch(cp -> "side_effect".equals(cp.getNodeId())));
        assertFalse(stored.get(stored.size() - 1).getStateData()
                .contains(NodeActorContext.KEY));

        CheckpointManager restartedManager =
                new CheckpointManager(new DbCheckpointStore(repository));
        var restarted = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, executionId,
                        restartedManager)).build());
        var result = restarted.invoke(GraphInput.resume(
                NodeActorContext.bind(ACTOR, Map.of())), config);

        assertEquals(1, sideEffects.get());
        assertEquals(1, completed.get());
        assertEquals("done", result.orElseThrow().data().get("result"));
        assertEquals(Long.class, result.orElseThrow().data().get("studentId").getClass());
    }

    @Test
    void educationDtoRunsLiveButItsCheckpointCannotResumeAsAnUntypedMap() throws Exception {
        AgentCheckpointRepository repository = mock(AgentCheckpointRepository.class);
        List<AgentCheckpointBO> stored = new ArrayList<>();
        doAnswer(invocation -> {
            AgentCheckpointBO checkpoint = invocation.getArgument(1);
            checkpoint.setTenantId(TENANT.value());
            stored.add(checkpoint);
            return null;
        }).when(repository).insert(eq(TENANT), any(AgentCheckpointBO.class));
        when(repository.selectLatestByExecutionId(eq(TENANT), any(String.class)))
                .thenAnswer(invocation -> {
                    String executionId = invocation.getArgument(1);
                    return stored.stream().filter(row -> executionId.equals(row.getExecutionId()))
                            .reduce((first, second) -> second).orElse(null);
                });
        KnowledgeDocumentDTO document = new KnowledgeDocumentDTO(2L, "lesson", "content",
                "TEXT", "teacher", List.of(1L));
        KnowledgeResponse knowledge = new KnowledgeResponse(1L, "k", "name", null,
                null, null, null, List.of(), List.of(), List.of(document));
        AtomicInteger readCount = new AtomicInteger();
        BaseNode query = new BaseNode(NodeConfig.builder().nodeId("query")
                .nodeName("query").nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("knowledge", knowledge);
                return output;
            }
        };
        BaseNode consume = new BaseNode(NodeConfig.builder().nodeId("consume")
                .nodeName("consume").nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                KnowledgeResponse restored = input.getParameter("knowledge");
                Long studentId = input.getParameter("studentId");
                assertEquals(knowledge, restored);
                assertEquals(document, restored.documents().get(0));
                assertEquals(42L, studentId);
                readCount.incrementAndGet();
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("result", "done");
                return output;
            }
        };
        Graph graph = Graph.builder().name("education-dto")
                .startNode("query").endNode("consume")
                .nodes(Map.of("query", query, "consume", consume))
                .edges(Map.of("query", List.of("consume")))
                .conditionalEdges(Map.of()).build();
        CheckpointManager manager = new CheckpointManager(new DbCheckpointStore(repository));
        var live = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, "live-dto", manager))
                .build());

        var liveResult = live.invoke(NodeActorContext.bind(ACTOR,
                Map.of("studentId", 42L)), RunnableConfig.builder().threadId("live-dto").build());

        assertEquals("done", liveResult.orElseThrow().data().get("result"));
        assertEquals(1, readCount.get());
        assertTrue(stored.stream().anyMatch(row -> "live-dto".equals(row.getExecutionId())
                && "__agent_nonresumable__".equals(row.getNodeId())));

        var interrupted = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, "paused-dto", manager))
                .interruptAfter("query").build());
        RunnableConfig pausedConfig = RunnableConfig.builder().threadId("paused-dto").build();
        interrupted.invoke(NodeActorContext.bind(ACTOR, Map.of("studentId", 42L)), pausedConfig);
        assertEquals("__agent_nonresumable__", manager.loadLatestCheckpoint(TENANT,
                "paused-dto").getNodeId());
        var restarted = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, "paused-dto", manager))
                .build());

        assertThrows(RuntimeException.class, () -> restarted.invoke(GraphInput.resume(
                NodeActorContext.bind(ACTOR, Map.of())), pausedConfig));
        assertEquals(1, readCount.get());
    }

    @Test
    void uncommittedNodeResultFailsClosedAfterRestartInsteadOfRepeatingSideEffect()
            throws Exception {
        String executionId = "crash-after-side-effect";
        AgentCheckpointRepository repository = mock(AgentCheckpointRepository.class);
        AtomicReference<AgentCheckpointBO> latest = new AtomicReference<>();
        AtomicReference<String> previousCheckpointId = new AtomicReference<>();
        AtomicBoolean failResultWrite = new AtomicBoolean(true);
        doAnswer(invocation -> {
            AgentCheckpointBO checkpoint = invocation.getArgument(1);
            if ("side_effect".equals(checkpoint.getNodeId())
                    && failResultWrite.getAndSet(false)) {
                throw new IllegalStateException("database write interrupted");
            }
            checkpoint.setTenantId(TENANT.value());
            latest.set(checkpoint);
            if ("__START__".equals(checkpoint.getNodeId())) {
                previousCheckpointId.set(checkpoint.getCheckpointId());
            }
            return null;
        }).when(repository).insert(eq(TENANT), any(AgentCheckpointBO.class));
        when(repository.selectLatestByExecutionId(TENANT, executionId))
                .thenAnswer(invocation -> latest.get());
        AtomicInteger sideEffects = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();
        Graph graph = graph(sideEffects, completed);
        RunnableConfig config = RunnableConfig.builder().threadId(executionId).build();
        var first = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, executionId,
                        new CheckpointManager(new DbCheckpointStore(repository)))).build());

        assertThrows(RuntimeException.class,
                () -> first.invoke(NodeActorContext.bind(ACTOR, Map.of()), config));
        assertEquals(1, sideEffects.get());
        assertEquals("__agent_node_started__", latest.get().getNodeId());

        var restarted = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, executionId,
                        new CheckpointManager(new DbCheckpointStore(repository)))).build());
        assertThrows(RuntimeException.class, () -> restarted.invoke(
                GraphInput.resume(NodeActorContext.bind(ACTOR, Map.of())), config));
        assertEquals(1, sideEffects.get());
        assertEquals(0, completed.get());
        assertThrows(RuntimeException.class, () -> restarted.invoke(
                GraphInput.resume(NodeActorContext.bind(ACTOR, Map.of())),
                config.withCheckPointId(previousCheckpointId.get())));
        assertEquals(1, sideEffects.get());
    }

    @Test
    void handledNodeFailureIsSupersededByAResultCheckpoint() throws Exception {
        String executionId = "handled-failure";
        AgentCheckpointRepository repository = mock(AgentCheckpointRepository.class);
        AtomicReference<AgentCheckpointBO> latest = new AtomicReference<>();
        doAnswer(invocation -> {
            AgentCheckpointBO checkpoint = invocation.getArgument(1);
            checkpoint.setTenantId(TENANT.value());
            latest.set(checkpoint);
            return null;
        }).when(repository).insert(eq(TENANT), any(AgentCheckpointBO.class));
        when(repository.selectLatestByExecutionId(TENANT, executionId))
                .thenAnswer(invocation -> latest.get());
        BaseNode failure = new BaseNode(NodeConfig.builder().nodeId("failure")
                .nodeName("failure").nodeType(NodeType.DEFAULT).timeout(0L)
                .errorStrategy("DEFAULT").build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                throw new IllegalStateException("temporary node failure");
            }
        };
        Graph graph = Graph.builder().name("handled-failure")
                .startNode("failure").endNode("finish")
                .nodes(Map.of("failure", failure,
                        "finish", node("finish", new AtomicInteger(), "result", "done")))
                .edges(Map.of("failure", List.of("finish")))
                .conditionalEdges(Map.of()).build();
        var compiled = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                .checkpointSaver(new AgentGraphCheckpointSaver(TENANT, executionId,
                        new CheckpointManager(new DbCheckpointStore(repository)))).build());

        var result = compiled.invoke(NodeActorContext.bind(ACTOR, Map.of()),
                RunnableConfig.builder().threadId(executionId).build());

        assertEquals("done", result.orElseThrow().data().get("result"));
        assertEquals("finish", latest.get().getNodeId());
    }

    @Test
    void syntheticPreToolCheckpointResumesAtTheToolNode() throws Exception {
        String executionId = "approval-resume";
        CheckpointManager manager = mock(CheckpointManager.class);
        Checkpoint pending = new Checkpoint(TENANT, executionId, "approval-checkpoint",
                "approval_pending", "side_effect",
                Map.of("tenantId", TENANT.value(), "userId", ACTOR.userId().value(),
                        "studentId", 42L, AgentGraphCheckpointSaver.APPROVAL_ID_KEY, "approval-1"));
        AtomicReference<Checkpoint> latest = new AtomicReference<>(pending);
        when(manager.loadLatestCheckpoint(TENANT, executionId))
                .thenAnswer(invocation -> latest.get());
        doAnswer(invocation -> {
            latest.set(invocation.getArgument(0));
            return null;
        }).when(manager).saveCheckpoint(any(Checkpoint.class));
        AtomicInteger sideEffects = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();
        BaseNode approvedTool = new BaseNode(NodeConfig.builder().nodeId("side_effect")
                .nodeName("side_effect").nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                assertEquals("approval-1", input.getParameter(
                        AgentGraphCheckpointSaver.APPROVAL_ID_KEY, null));
                sideEffects.incrementAndGet();
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("effect", "done");
                return output;
            }
        };
        Graph graph = Graph.builder().name("approval-resume")
                .startNode("side_effect").endNode("finish")
                .nodes(Map.of("side_effect", approvedTool,
                        "finish", node("finish", completed, "result", "done")))
                .edges(Map.of("side_effect", List.of("finish")))
                .conditionalEdges(Map.of()).build();
        var compiled = StateGraphBuilder.fromGraph(graph)
                .build(CompileConfig.builder().checkpointSaver(
                        new AgentGraphCheckpointSaver(TENANT, executionId, manager)).build());

        var result = compiled.invoke(GraphInput.resume(NodeActorContext.bind(ACTOR, Map.of())),
                RunnableConfig.builder().threadId(executionId)
                        .checkPointId(pending.getCheckpointId()).build());

        assertEquals("done", result.orElseThrow().data().get("result"));
        assertEquals(1, sideEffects.get());
        assertEquals(1, completed.get());
    }

    private Graph graph(AtomicInteger sideEffects, AtomicInteger completed) {
        return Graph.builder().name("restart")
                .startNode("side_effect").endNode("finish")
                .nodes(Map.of(
                        "side_effect", node("side_effect", sideEffects, "effect", "done"),
                        "finish", node("finish", completed, "result", "done")))
                .edges(Map.of("side_effect", List.of("finish")))
                .conditionalEdges(Map.of()).build();
    }

    private BaseNode node(String id, AtomicInteger calls, String key, String value) {
        return new BaseNode(NodeConfig.builder().nodeId(id).nodeName(id)
                .nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                assertEquals(ACTOR, NodeActorContext.require(input.toMap()));
                assertEquals(Long.class, input.toMap().get("tenantId").getClass());
                assertEquals(Long.class, input.toMap().get("userId").getClass());
                if (input.toMap().containsKey("studentId")) {
                    assertEquals(Long.class, input.toMap().get("studentId").getClass());
                }
                calls.incrementAndGet();
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData(key, value);
                return output;
            }
        };
    }
}

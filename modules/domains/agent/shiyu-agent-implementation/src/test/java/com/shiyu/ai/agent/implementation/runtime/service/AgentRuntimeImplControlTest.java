package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionStartedEvent;
import com.shiyu.ai.agent.implementation.runtime.service.AgentRuntimeImpl;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunContext;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunSource;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.domain.model.AgentCheckpointBO;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.graph.Graph;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallNode;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallNode;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiRunRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatResponse;
import com.shiyu.ai.tooling.contract.api.ToolService;
import com.shiyu.ai.kernel.error.DomainAccessDeniedException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import reactor.core.Disposable;
import reactor.core.publisher.Sinks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证 智能体 Runtime Impl Control 相关功能、边界条件、异常路径和协作行为。
 */
class AgentRuntimeImplControlTest {

    @Test
    void highRiskToolWaitsForApprovalWithoutFailingItsExecution() {
        AiRuntimeService runs = new AiRuntimeService(
                new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        ToolApprovalService approvals =
                new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolService tools = mock(ToolService.class);
        ToolCallNode tool = ToolCallNode.builder()
                .toolService(tools)
                .executionPipeline(new ToolExecutionPipeline(runs, approvals))
                .config(ToolCallConfig.builder().nodeId("block").nodeName("block")
                        .toolName("filesystem.write").toolType("HIGH")
                        .timeout(0L).build())
                .build();
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), tool,
                mock(EventPublisher.class), runs);

        Execution execution = runtime.execute(ACTOR, "agent-1", Map.of("path", "a"));

        assertEquals(ExecutionStatus.PAUSED, execution.getStatus());
        AiRun run = runs.requireExecutionRun(execution.getExecutionId(),
                ACTOR.tenantId(), ACTOR.userId().value());
        assertEquals(AiRunStatus.RUNNING, run.status());
        assertEquals(1, approvals.list(run.id(), ACTOR.tenantId(), ACTOR.userId().value()).size());
        verify(tools, never()).execute(anyString(), any());
    }

    @Test
    void streamingHighRiskToolReportsPauseInsteadOfFailure() {
        AiRuntimeService runs = new AiRuntimeService(
                new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        ToolApprovalService approvals =
                new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolService tools = mock(ToolService.class);
        ToolCallNode tool = ToolCallNode.builder()
                .toolService(tools)
                .executionPipeline(new ToolExecutionPipeline(runs, approvals))
                .config(ToolCallConfig.builder().nodeId("block").nodeName("block")
                        .toolName("filesystem.write").toolType("HIGH").build())
                .build();
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), tool,
                mock(EventPublisher.class), runs);

        List<Map<String, Object>> events = runtime.executeStream(ACTOR, "agent-1",
                Map.of("path", "a"))
                .takeUntil(event -> "PAUSED".equals(event.get("status")))
                .collectList().block(java.time.Duration.ofSeconds(5));

        assertEquals("STARTED", events.getFirst().get("status"));
        assertEquals("PAUSED", events.getLast().get("status"));
        assertTrue(events.getLast().containsKey("approvalId"));
        assertEquals(ExecutionStatus.PAUSED, runtime.getStatus(ACTOR,
                (String) events.getLast().get("executionId")));
        assertEquals(1, approvals.list((String) events.getFirst().get("runtimeRunId"),
                ACTOR.tenantId(), ACTOR.userId().value()).size());
        verify(tools, never()).execute(anyString(), any());
    }

    @Test
    void approvedHighRiskToolResumesTheSameExecutionOnce() {
        AiRuntimeService runs = new AiRuntimeService(
                new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        ToolApprovalService approvals =
                new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolService tools = mock(ToolService.class);
        when(tools.execute(eq("filesystem.write"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(true, "written", null));
        ToolCallNode tool = ToolCallNode.builder()
                .toolService(tools)
                .executionPipeline(new ToolExecutionPipeline(runs, approvals))
                .config(ToolCallConfig.builder().nodeId("block").nodeName("block")
                        .toolName("filesystem.write").toolType("HIGH")
                        .timeout(0L).build())
                .build();
        AgentExecutionRepository executions = mock(AgentExecutionRepository.class);
        AtomicReference<AgentExecutionBO> savedExecution = new AtomicReference<>();
        doAnswer(invocation -> {
            savedExecution.set(invocation.getArgument(1));
            return null;
        }).when(executions).insert(any(TenantId.class), any(AgentExecutionBO.class));
        doAnswer(invocation -> {
            savedExecution.set(invocation.getArgument(1));
            return null;
        }).when(executions).update(any(TenantId.class), any(AgentExecutionBO.class));
        when(executions.selectByExecutionId(any(TenantId.class), anyString()))
                .thenAnswer(invocation -> savedExecution.get());
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        List<AgentCheckpointBO> stored = new CopyOnWriteArrayList<>();
        doAnswer(invocation -> {
            AgentCheckpointBO checkpoint = invocation.getArgument(1);
            checkpoint.setTenantId(ACTOR.tenantId().value());
            stored.add(checkpoint);
            return null;
        }).when(checkpoints).insert(any(TenantId.class), any(AgentCheckpointBO.class));
        when(checkpoints.selectLatestByExecutionId(any(TenantId.class), anyString()))
                .thenAnswer(invocation -> stored.isEmpty() ? null : stored.getLast());
        doAnswer(invocation -> {
            stored.clear();
            return null;
        }).when(checkpoints).deleteByExecutionId(any(TenantId.class), anyString());
        AgentRuntimeImpl runtime = newRuntime(executions, tool, mock(EventPublisher.class),
                runs, checkpoints, approvals);

        Execution pending = runtime.execute(ACTOR, "agent-1", Map.of("path", "a"));
        AiRun run = runs.requireExecutionRun(pending.getExecutionId(),
                ACTOR.tenantId(), ACTOR.userId().value());
        var approval = approvals.list(run.id(), ACTOR.tenantId(), ACTOR.userId().value()).getFirst();
        assertEquals(ExecutionStatus.PAUSED, pending.getStatus());
        assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, pending.getExecutionId()));
        assertEquals(ExecutionStatus.PAUSED, pending.getStatus());

        var otherRunApproval = approvals.request("another-run", ACTOR.tenantId(),
                ACTOR.userId().value(), "filesystem.write", "different invocation");
        approvals.decide(otherRunApproval.id(), ACTOR.tenantId(), ACTOR.userId().value(),
                ToolApprovalStatus.APPROVED);
        AgentCheckpointBO pendingCheckpoint = stored.getLast();
        String originalState = pendingCheckpoint.getStateData();
        assertTrue(originalState.contains(approval.id()));
        pendingCheckpoint.setStateData(originalState.replace(approval.id(), otherRunApproval.id()));
        AgentRuntimeImpl restarted = newRuntime(executions, tool, mock(EventPublisher.class),
                runs, checkpoints, approvals);
        assertThrows(IllegalStateException.class,
                () -> restarted.resume(ACTOR, pending.getExecutionId()));
        verify(tools, never()).execute(eq("filesystem.write"), any());
        assertEquals(ExecutionStatus.PAUSED,
                restarted.getStatus(ACTOR, pending.getExecutionId()));
        pendingCheckpoint.setStateData(originalState);

        approvals.decide(approval.id(), ACTOR.tenantId(), ACTOR.userId().value(),
                ToolApprovalStatus.APPROVED);
        Execution completed = restarted.resume(ACTOR, pending.getExecutionId());
        assertEquals(ExecutionStatus.COMPLETED, completed.getStatus());
        assertEquals("written", completed.getOutput().get("toolResult"));
        assertEquals(AiRunStatus.COMPLETED, runs.requireRun(run.id(), ACTOR.tenantId(),
                ACTOR.userId().value()).status());
        assertEquals(ToolApprovalStatus.COMPLETED, approvals.require(approval.id(),
                ACTOR.tenantId(), ACTOR.userId().value()).status());
        verify(tools).execute(eq("filesystem.write"), any());
        assertThrows(IllegalStateException.class,
                () -> restarted.resume(ACTOR, pending.getExecutionId()));
    }

    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(9), new UserId(7), false);

    private Disposable disposable;

    @Test
    void modelChunksReachRuntimeSubscribersBeforeModelCompletes() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        Sinks.Many<ChatResponse> model = Sinks.many().unicast().onBackpressureBuffer();
        when(engine.stream(any())).thenReturn(model.asFlux());
        LlmCallNode node = LlmCallNode.builder().chatEngine(engine)
                .config(LlmCallConfig.builder().nodeId("block").stream(true).build()).build();
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), node);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();
        CountDownLatch firstChunk = new CountDownLatch(1);
        CountDownLatch terminal = new CountDownLatch(1);
        disposable = runtime.executeStream(ACTOR, "agent-1", Map.of("query", "hello"))
                .subscribe(event -> {
                    events.add(event);
                    if ("DELTA".equals(event.get("status"))) firstChunk.countDown();
                }, error -> terminal.countDown(), terminal::countDown);

        verify(engine, timeout(2000)).stream(any());
        model.tryEmitNext(ChatResponse.builder().eventType("DELTA").content("answer").build());
        try {
            assertTrue(firstChunk.await(2, TimeUnit.SECONDS), "model chunk must reach subscriber");
            assertEquals("answer", events.stream()
                    .filter(event -> "DELTA".equals(event.get("status")))
                    .findFirst().orElseThrow().get("content"));
        } finally {
            model.tryEmitComplete();
        }
        assertTrue(terminal.await(2, TimeUnit.SECONDS));
        Map<String, Object> completed = events.stream()
                .filter(event -> "COMPLETED".equals(event.get("status")))
                .findFirst().orElseThrow();
        assertEquals("answer", ((Map<?, ?>) completed.get("output")).get("content"));
        assertFalse(((Map<?, ?>) completed.get("output")).containsKey("_streaming_generator"));
    }

    @AfterEach
    void cleanup() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Test
    void suppliesAuthorizedAgentIdentityToNodeHistory() {
        var history = mock(com.shiyu.ai.agent.contract.ExecutionHistoryService.class);
        ReleasingNode node = new ReleasingNode();
        node.setExecutionHistoryService(history);
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), node);
        runtime.executeStream(ACTOR, "agent-1", Map.of("agentId", "spoofed", "version", "spoofed"))
                .collectList().block(java.time.Duration.ofSeconds(5));
        verify(history).startExecution(eq(ACTOR), eq("agent-1"), eq("v1"),
                isNull(), eq("block"), anyString(), anyString());
    }

    @Test
    void bindsTenantWhenStreamMovesToBackgroundThread() {
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        when(checkpoints.selectLatestByExecutionId(eq(ACTOR.tenantId()), anyString()))
                .thenAnswer(invocation -> {
                    assertEquals(ACTOR.tenantId(), TenantScope.require());
                    return null;
                });
        doAnswer(invocation -> {
            assertEquals(ACTOR.tenantId(), TenantScope.require());
            return null;
        }).when(checkpoints).insert(eq(ACTOR.tenantId()), any(AgentCheckpointBO.class));
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class),
                new ReleasingNode(), mock(EventPublisher.class), null, checkpoints);
        List<Map<String, Object>> events = runtime.executeStream(ACTOR, "agent-1", Map.of())
                .collectList().block(java.time.Duration.ofSeconds(5));
        assertTrue(events.stream().anyMatch(event -> "COMPLETED".equals(event.get("status"))));
        assertTrue(TenantScope.current().isEmpty());
    }

    @Test
    void pauseThenResumeLetsStreamCompleteAndPersistsSuccess() throws Exception {
        BlockingNode node = new BlockingNode();
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        AgentRuntimeImpl runtime = newRuntime(repository, node);
        CountDownLatch terminal = new CountDownLatch(1);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();

        disposable =
                runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello"))
                        .subscribe(events::add, error -> terminal.countDown(), terminal::countDown);

        assertTrue(node.entered.await(5, TimeUnit.SECONDS), "graph node should start");
        String executionId = recorder.awaitExecutionId();
        assertEquals("STARTED", events.getFirst().get("status"));
        assertEquals(executionId, events.getFirst().get("executionId"));

        runtime.pause(ACTOR, executionId);
        assertEquals(ExecutionStatus.PAUSED, runtime.getStatus(ACTOR, executionId));

        runtime.resume(ACTOR, executionId);
        assertEquals(ExecutionStatus.RUNNING, runtime.getStatus(ACTOR, executionId));

        node.release.countDown();
        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should complete after resume");
        assertTrue(
                events.stream().anyMatch(event -> "COMPLETED".equals(event.get("status"))),
                "stream should emit a COMPLETED terminal event");
        verify(repository, timeout(5000).atLeastOnce())
                .update(
                        any(TenantId.class),
                        argThat(
                                bo ->
                                        executionId.equals(bo.getExecutionId())
                                                && Integer.valueOf(1).equals(bo.getStatus())));
    }

    @Test
    void cancelDuringExecutionEmitsCancelledTerminalAndPersistsCancelled() throws Exception {
        BlockingNode node = new BlockingNode();
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        AgentRuntimeImpl runtime = newRuntime(repository, node);
        CountDownLatch terminal = new CountDownLatch(1);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();

        disposable =
                runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello"))
                        .subscribe(events::add, error -> terminal.countDown(), terminal::countDown);

        assertTrue(node.entered.await(5, TimeUnit.SECONDS), "graph node should start");
        String executionId = recorder.awaitExecutionId();

        runtime.cancel(ACTOR, executionId);
        assertEquals(ExecutionStatus.CANCELLED, runtime.getStatus(ACTOR, executionId));

        node.release.countDown();
        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should terminate after cancel");
        assertTrue(
                events.stream().anyMatch(event -> "CANCELLED".equals(event.get("status"))),
                "stream should emit a CANCELLED terminal event");
        verify(repository, timeout(5000).atLeastOnce())
                .update(
                        any(TenantId.class),
                        argThat(
                                bo ->
                                        executionId.equals(bo.getExecutionId())
                                                && Integer.valueOf(4).equals(bo.getStatus())));
    }

    @Test
    void clientDisconnectCancelsRunningExecutionAndPersistsCancelled() throws Exception {
        BlockingNode node = new BlockingNode();
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        AgentRuntimeImpl runtime = newRuntime(repository, node);

        disposable =
                runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello")).subscribe();

        assertTrue(node.entered.await(5, TimeUnit.SECONDS), "graph node should start");
        String executionId = recorder.awaitExecutionId();
        disposable.dispose();
        node.release.countDown();

        verify(repository, timeout(5000).atLeastOnce())
                .update(
                        any(TenantId.class),
                        argThat(
                                bo ->
                                        executionId.equals(bo.getExecutionId())
                                                && Integer.valueOf(4).equals(bo.getStatus())));
    }

    @Test
    void persistenceFailureFailsTheExecutionCommand() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        doThrow(new IllegalStateException("database unavailable"))
                .when(repository)
                .insert(any(TenantId.class), any(AgentExecutionBO.class));
        AgentRuntimeImpl runtime = newRuntime(repository, new BlockingNode());

        assertThrows(
                IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("tenantId", 999, "userId", 999)));
    }

    @Test
    void rethrowsPersistenceFailureAfterExecutionHasStarted() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        java.util.concurrent.atomic.AtomicInteger writes =
                new java.util.concurrent.atomic.AtomicInteger();
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);
        doAnswer(
                        invocation -> {
                            if (writes.incrementAndGet() > 1) {
                                throw new IllegalStateException("database unavailable after start");
                            }
                            return null;
                        })
                .when(repository)
                .insert(any(TenantId.class), any(AgentExecutionBO.class));
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        assertThrows(
                IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("message", "hello")));
        verify(publisher)
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionStartedEvent
                                        .class));
        verify(publisher, never())
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent
                                        .class));
    }

    @Test
    void retriesCompletedTerminalPersistenceWithoutPublishingFalseFailure() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        java.util.concurrent.atomic.AtomicInteger terminalWrites =
                new java.util.concurrent.atomic.AtomicInteger();
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);
        doAnswer(
                        invocation -> {
                            AgentExecutionBO saved = invocation.getArgument(1);
                            if (Integer.valueOf(1).equals(saved.getStatus())
                                    && terminalWrites.incrementAndGet() == 1) {
                                throw new IllegalStateException("terminal write failed");
                            }
                            return null;
                        })
                .when(repository)
                .insert(any(TenantId.class), any(AgentExecutionBO.class));
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        assertEquals(ExecutionStatus.COMPLETED,
                runtime.execute(ACTOR, "agent-1", Map.of("message", "hello")).getStatus());
        verify(publisher)
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionStartedEvent
                                        .class));
        verify(publisher)
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionCompletedEvent
                                        .class));
        verify(publisher, never())
                .publish(any(com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent.class));
    }

    @Test
    void usesDatabaseLoaderWhenScopedAndSystemCachesMiss() {
        AgentAdminRepository adminRepository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cacheManager = new AgentCacheManager(adminRepository, loader);
        ReleasingNode node = new ReleasingNode();
        Graph graph =
                Graph.builder()
                        .name("loader-test")
                        .startNode("block")
                        .endNode("block")
                        .nodes(Map.of("block", node))
                        .edges(Map.of())
                        .conditionalEdges(Map.of())
                        .build();
        AgentVersion version = AgentVersion.builder().versionNumber("v1").graph(graph).build();
        AgentDefinition definition =
                AgentDefinition.builder().agentId("agent-1").currentVersion("v1").build();
        definition.addVersion(version);
        when(loader.loadFromDb(ACTOR, "agent-1")).thenReturn(definition);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);

        AgentRuntimeImpl runtime =
                new AgentRuntimeImpl(
                        cacheManager,
                        loader,
                        repository,
                        mock(AgentCheckpointRepository.class),
                        mock(EventPublisher.class));
        assertEquals(
                ExecutionStatus.COMPLETED,
                runtime.execute(ACTOR, "agent-1", Map.of("message", "loaded")).getStatus());
        verify(loader).loadFromDb(ACTOR, "agent-1");
    }

    @Test
    void rejectsUnknownControlOperations() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        assertThrows(IllegalStateException.class, () -> runtime.pause(ACTOR, "missing"));
        assertThrows(IllegalStateException.class, () -> runtime.resume(ACTOR, "missing"));
        assertThrows(IllegalStateException.class, () -> runtime.cancel(ACTOR, "missing"));
    }

    @Test
    void synchronousExecutionPublishesCompletionAndPersistsOutput() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        Execution result =
                runtime.execute(ACTOR, "agent-1", Map.of("message", "hello", "sessionId", "s-1"));

        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals("s-1", result.getSessionId());
        assertTrue(result.getOutput() != null);
        verify(publisher)
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionStartedEvent
                                        .class));
        verify(publisher)
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionCompletedEvent
                                        .class));
        assertEquals(result.getExecutionId(), recorder.stored.getFirst().getExecutionId());
    }

    @Test
    void failedNodeOutputCannotCompleteSynchronousExecution() {
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class),
                new FailedOutputNode());

        Execution result = runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"));

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
    }

    @Test
    void failedStartEventPersistsFailureBeforeReturningTheOriginalError() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);
        EventPublisher publisher = mock(EventPublisher.class);
        IllegalStateException startFailure = new IllegalStateException("event bus unavailable");
        doThrow(startFailure).when(publisher).publish(any(AgentExecutionStartedEvent.class));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        assertSame(startFailure, assertThrows(IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"))));
        verify(repository).insert(eq(ACTOR.tenantId()), argThat(bo ->
                Integer.valueOf(2).equals(bo.getStatus())));
        verify(publisher).publish(any(AgentExecutionFailedEvent.class));
    }

    @Test
    void failedStartKeepsTheOriginalErrorWhenFailureCleanupAlsoFails() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);
        IllegalStateException writeFailure = new IllegalStateException("failure write unavailable");
        doAnswer(invocation -> {
            AgentExecutionBO value = invocation.getArgument(1);
            if (Integer.valueOf(2).equals(value.getStatus())) throw writeFailure;
            return null;
        }).when(repository).insert(any(TenantId.class), any(AgentExecutionBO.class));
        EventPublisher publisher = mock(EventPublisher.class);
        IllegalStateException startFailure = new IllegalStateException("start event unavailable");
        IllegalStateException failEventFailure = new IllegalStateException("failure event unavailable");
        doThrow(startFailure).when(publisher).publish(any(AgentExecutionStartedEvent.class));
        doThrow(failEventFailure).when(publisher).publish(any(AgentExecutionFailedEvent.class));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        assertSame(startFailure, assertThrows(IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"))));
        assertTrue(List.of(startFailure.getSuppressed()).contains(writeFailure));
        assertTrue(List.of(startFailure.getSuppressed()).contains(failEventFailure));
    }

    @Test
    void failedNodeOutputCannotCompleteStreamingExecution() {
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class),
                new FailedOutputNode());

        assertThrows(RuntimeException.class, () -> runtime.executeStream(ACTOR, "agent-1",
                Map.of("message", "hello")).collectList().block(java.time.Duration.ofSeconds(5)));
    }

    @Test
    void synchronousGraphFailureReturnsFailedExecutionAndPublishesFailure() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new FailingNode(), publisher);

        Execution result = runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"));

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("执行异常"));
        verify(publisher)
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent
                                        .class));
    }

    @Test
    void streamFailurePublishesFailureAndPropagatesTheOriginalError() throws Exception {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new FailingNode(), publisher);
        CountDownLatch terminal = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        disposable =
                runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello"))
                        .subscribe(
                                ignored -> {},
                                error -> {
                                    failure.set(error);
                                    terminal.countDown();
                                },
                                terminal::countDown);

        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should terminate with an error");
        assertTrue(failure.get() != null && failure.get().getMessage() != null);
        verify(publisher, timeout(5000))
                .publish(
                        any(
                                com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent
                                        .class));
    }

    @Test
    void historyAndUserHistoryEnforceTenantAndUserOwnership() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO bo = new AgentExecutionBO();
        bo.setExecutionId("history-1");
        bo.setAgentId("agent-1");
        bo.setVersion("v1");
        bo.setTenantId(9L);
        bo.setUserId(7L);
        bo.setSessionId("7");
        bo.setStatus(1);
        when(repository.selectByAgentId(new TenantId(9), "agent-1", 100)).thenReturn(List.of(bo));
        when(repository.selectByAgentIdAndUserId(new TenantId(9), "agent-1", 7L, 100))
                .thenReturn(List.of(bo));
        when(repository.selectByUserId(new TenantId(9), 7L, 10)).thenReturn(List.of(bo));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        assertEquals(1, runtime.getHistory(ACTOR, "agent-1", 100).size());
        assertEquals(1, runtime.getUserHistory(ACTOR, 7L, 10).size());
        assertThrows(IllegalStateException.class, () -> runtime.getUserHistory(ACTOR, 8L, 10));
        ActorContext admin = new ActorContext(new TenantId(9), new UserId(99), true);
        assertEquals(1, runtime.getUserHistory(admin, 7L, 10).size());
    }

    @Test
    void cancellationFromFinalNodeEventCannotBecomeCompleted() throws Exception {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        CountDownLatch terminal = new CountDownLatch(1);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();
        java.util.concurrent.atomic.AtomicReference<String> executionId =
                new java.util.concurrent.atomic.AtomicReference<>();

        disposable = runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello"))
                .subscribe(event -> {
                    events.add(event);
                    if ("STARTED".equals(event.get("status"))) {
                        executionId.set((String) event.get("executionId"));
                    }
                    if (event.containsKey("node")) runtime.cancel(ACTOR, executionId.get());
                }, error -> terminal.countDown(), terminal::countDown);

        assertTrue(terminal.await(5, TimeUnit.SECONDS));
        assertTrue(executionId.get() != null, "stream should expose its execution ID");
        verify(repository, timeout(5000).atLeastOnce()).insert(eq(ACTOR.tenantId()),
                argThat(bo -> executionId.get().equals(bo.getExecutionId())
                        && Integer.valueOf(4).equals(bo.getStatus())));
        assertFalse(events.stream().anyMatch(event -> "COMPLETED".equals(event.get("status"))));
    }

    @Test
    void trustedActorStaysOutOfPersistedExecutionAndStreamEvents() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        ActorContext forged = new ActorContext(new TenantId(99), new UserId(88), false);
        Map<String, Object> input = Map.of("message", "hello",
                "__knowledgeAccessContext", forged);

        Execution result = runtime.execute(ACTOR, "agent-1", input);
        assertFalse(result.getInput().containsKey("__knowledgeAccessContext"));
        assertFalse(result.getOutput().containsKey("__knowledgeAccessContext"));

        List<Map<String, Object>> events = runtime.executeStream(ACTOR, "agent-1", input)
                .collectList().block();
        assertTrue(events != null && !events.isEmpty());
        for (Map<String, Object> event : events) {
            Object state = event.get("state");
            if (state instanceof Map<?, ?> map) {
                assertFalse(map.containsKey("__knowledgeAccessContext"));
            }
            Object output = event.get("output");
            if (output instanceof Map<?, ?> map) {
                assertFalse(map.containsKey("__knowledgeAccessContext"));
            }
        }
    }

    @Test
    void agentHistoryExcludesOtherUsersExecutionsBeforeApplyingLimit() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO owned = persisted("owned", 1, LocalDateTime.now(), null);
        AgentExecutionBO foreign = persisted("foreign", 1, LocalDateTime.now(), null);
        foreign.setUserId(8L);
        when(repository.selectByAgentIdAndUserId(new TenantId(9), "agent-1", 7L, 1))
                .thenReturn(List.of(owned));
        when(repository.selectByAgentId(new TenantId(9), "agent-1", 1))
                .thenReturn(List.of(foreign));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        assertEquals(List.of("owned"), runtime.getHistory(ACTOR, "agent-1", 1).stream()
                .map(Execution::getExecutionId).toList());
        ActorContext admin = new ActorContext(new TenantId(9), new UserId(99), true);
        assertEquals(List.of("foreign"), runtime.getHistory(admin, "agent-1", 1).stream()
                .map(Execution::getExecutionId).toList());
    }

    @Test
    void executionDetailAndControlRejectAnotherUserInTheSameTenant() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO foreign = persisted("foreign", 3, LocalDateTime.now(), null);
        foreign.setUserId(8L);
        when(repository.selectByExecutionId(new TenantId(9), "foreign")).thenReturn(foreign);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        assertThrows(DomainAccessDeniedException.class,
                () -> runtime.getExecution(ACTOR, "foreign"));
        assertThrows(DomainAccessDeniedException.class,
                () -> runtime.getStatus(ACTOR, "foreign"));
        assertThrows(DomainAccessDeniedException.class,
                () -> runtime.resume(ACTOR, "foreign"));
        assertThrows(DomainAccessDeniedException.class,
                () -> runtime.cancel(ACTOR, "foreign"));
        ActorContext admin = new ActorContext(new TenantId(9), new UserId(99), true);
        assertEquals("foreign", runtime.getExecution(admin, "foreign").getExecutionId());
    }

    @Test
    void userHistoryRejectsUnexpectedForeignRowsEvenIfRepositoryReturnsThem() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO owned = persisted("owned", 1, LocalDateTime.now(), null);
        AgentExecutionBO foreign = persisted("foreign", 1, LocalDateTime.now(), null);
        foreign.setUserId(8L);
        foreign.setSessionId("7");
        when(repository.selectByUserId(new TenantId(9), 7L, 10))
                .thenReturn(List.of(foreign, owned));
        when(repository.selectByUserId(new TenantId(9), 7L, 1))
                .thenReturn(List.of(foreign, owned));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        assertEquals(List.of("owned"), runtime.getUserHistory(ACTOR, 7L, 10).stream()
                .map(Execution::getExecutionId).toList());
        assertEquals(List.of("owned"), runtime.getUserHistory(ACTOR, 7L, 0).stream()
                .map(Execution::getExecutionId).toList());

        assertThrows(IllegalStateException.class,
                () -> runtime.getUserHistory(ACTOR, 8L, 10));
        verify(repository, never()).selectByUserId(new TenantId(9), 8L, 10);

        ActorContext admin = new ActorContext(new TenantId(9), new UserId(99), true);
        when(repository.selectByUserId(new TenantId(9), 8L, 10))
                .thenReturn(List.of(owned, foreign));
        assertEquals(List.of("foreign"), runtime.getUserHistory(admin, 8L, 10).stream()
                .map(Execution::getExecutionId).toList());
    }

    @Test
    void userHistoryIncludesExecutionsWithIndependentConversationSessionIds() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO owned = persisted("custom-session", 1, LocalDateTime.now(), null);
        owned.setSessionId("conversation-42");
        when(repository.selectByUserId(new TenantId(9), 7L, 10)).thenReturn(List.of(owned));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        assertEquals(List.of("custom-session"), runtime.getUserHistory(ACTOR, 7L, 10).stream()
                .map(Execution::getExecutionId).toList());
    }

    @Test
    void rebuildsPersistedExecutionsAndDerivesTerminalStatusFromEndTime() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO running = persisted("stored-1", 0, null, null);
        when(repository.selectByExecutionId(new TenantId(9), "stored-1")).thenReturn(running);
        when(repository.selectByExecutionId(new TenantId(10), "stored-1")).thenReturn(running);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        assertEquals(ExecutionStatus.RUNNING, runtime.getStatus(ACTOR, "stored-1"));
        assertEquals("stored-1", runtime.getExecution(ACTOR, "stored-1").getExecutionId());

        running.setEndTime(LocalDateTime.now());
        running.setErrorMessage(null);
        assertEquals(ExecutionStatus.COMPLETED, runtime.getStatus(ACTOR, "stored-1"));
        running.setErrorMessage("failed");
        assertEquals(ExecutionStatus.FAILED, runtime.getStatus(ACTOR, "stored-1"));

        when(repository.selectByExecutionId(new TenantId(9), "missing")).thenReturn(null);
        assertNull(runtime.getStatus(ACTOR, "missing"));
        assertNull(runtime.getExecution(ACTOR, "missing"));
        assertThrows(
                DomainAccessDeniedException.class,
                () ->
                        runtime.getExecution(
                                new ActorContext(new TenantId(10), new UserId(7), false),
                                "stored-1"));
    }

    @Test
    void rejectsPersistedPauseWithoutCheckpointAndCancelsPersistedExecution() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO paused = persisted("stored-2", 3, null, null);
        when(repository.selectByExecutionId(new TenantId(9), "stored-2")).thenReturn(paused);
        when(repository.selectByAgentId(new TenantId(9), "agent-1", 1)).thenReturn(List.of(paused));
        when(repository.selectByAgentIdAndUserId(new TenantId(9), "agent-1", 7L, 1))
                .thenReturn(List.of(paused));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        IllegalStateException rejected = assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, "stored-2"));
        assertTrue(rejected.getMessage().contains("无可恢复检查点"));
        verify(repository, never()).update(any(TenantId.class),
                argThat(value -> "stored-2".equals(value.getExecutionId())));
        assertEquals(1, runtime.getHistory(ACTOR, "agent-1", 0).size());

        AgentExecutionBO cancellable = persisted("stored-3", 0, null, null);
        when(repository.selectByExecutionId(new TenantId(9), "stored-3")).thenReturn(cancellable);
        runtime.cancel(ACTOR, "stored-3");
        verify(repository)
                .update(
                        any(TenantId.class),
                        argThat(value -> Integer.valueOf(4).equals(value.getStatus())));
    }

    @Test
    void cancellingPersistedExecutionRemovesItsCheckpoints() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO persisted = persisted("stored-cancel", 3, null, null);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "stored-cancel"))
                .thenReturn(persisted);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        runtime.cancel(ACTOR, "stored-cancel");

        verify(repository).update(eq(ACTOR.tenantId()),
                argThat(value -> Integer.valueOf(4).equals(value.getStatus())));
        verify(checkpoints).deleteByExecutionId(ACTOR.tenantId(), "stored-cancel");
    }

    @Test
    void administratorCancellationPreservesExecutionOwner() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO owned = persisted("owner-preserved", 3, null, null);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "owner-preserved"))
                .thenReturn(owned);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        ActorContext administrator = new ActorContext(ACTOR.tenantId(), new UserId(99), true);

        runtime.cancel(administrator, "owner-preserved");

        verify(repository).update(eq(ACTOR.tenantId()),
                argThat(value -> "owner-preserved".equals(value.getExecutionId())
                        && Long.valueOf(7).equals(value.getUserId())
                        && Integer.valueOf(4).equals(value.getStatus())));
    }

    @Test
    void persistedPauseWithoutCheckpointDoesNotReplayUntrustedInput() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO paused = persisted("resume-actor", 3, null, null);
        paused.setInputData("{\"tenantId\":9,\"userId\":88,\"__knowledgeAccessContext\":{\"tenantId\":99}}");
        when(repository.selectByExecutionId(new TenantId(9), "resume-actor"))
                .thenReturn(paused);
        BaseNode actorNode = new BaseNode(NodeConfig.builder()
                .nodeId("block").nodeName("block").nodeType(NodeType.DEFAULT)
                .timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                ActorContext trusted = input.getParameter("__knowledgeAccessContext");
                if (trusted == null) throw new IllegalStateException("trusted actor missing");
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("callerTenant", trusted.tenantId().value());
                output.addData("callerUser", trusted.userId().value());
                return output;
            }
        };
        AgentRuntimeImpl runtime = newRuntime(repository, actorNode);

        assertThrows(IllegalStateException.class, () -> runtime.resume(ACTOR, "resume-actor"));
        verify(repository, never()).update(any(TenantId.class),
                argThat(value -> "resume-actor".equals(value.getExecutionId())));
    }

    @Test
    void persistedPauseWithFinalCheckpointUsesSavedOutputWithoutInvokingNode() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO paused = persisted("saved-result", 3, null, null);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "saved-result"))
                .thenReturn(paused);
        AgentCheckpointBO finalCheckpoint = new AgentCheckpointBO();
        finalCheckpoint.setTenantId(9L);
        finalCheckpoint.setExecutionId("saved-result");
        finalCheckpoint.setNodeId("graph_final");
        finalCheckpoint.setStateData("{\"answer\":\"saved\",\"__knowledgeAccessContext\":\"forged\"}");
        when(checkpoints.selectLatestByExecutionId(ACTOR.tenantId(), "saved-result"))
                .thenReturn(finalCheckpoint);
        BaseNode forbiddenNode = new BaseNode(NodeConfig.builder()
                .nodeId("block").nodeName("block").nodeType(NodeType.DEFAULT)
                .timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                throw new AssertionError("completed graph must not replay");
            }
        };
        AgentRuntimeImpl runtime = newRuntime(repository, forbiddenNode,
                mock(EventPublisher.class), null, checkpoints);

        Execution result = runtime.resume(ACTOR, "saved-result");

        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals(Map.of("answer", "saved"), result.getOutput());
    }

    @Test
    void persistedPauseRejectsCheckpointThatDiffersFromExecutionRecord() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO paused = persisted("checkpoint-drift", 3, null, null);
        paused.setLastCheckpointId("recorded-checkpoint");
        when(repository.selectByExecutionId(ACTOR.tenantId(), "checkpoint-drift"))
                .thenReturn(paused);
        AgentCheckpointBO latest = new AgentCheckpointBO();
        latest.setTenantId(9L);
        latest.setExecutionId("checkpoint-drift");
        latest.setCheckpointId("other-checkpoint");
        latest.setNodeId("graph_final");
        latest.setStateData("{\"answer\":\"untrusted\"}");
        when(checkpoints.selectLatestByExecutionId(ACTOR.tenantId(), "checkpoint-drift"))
                .thenReturn(latest);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        IllegalStateException rejected = assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, "checkpoint-drift"));

        assertTrue(rejected.getMessage().contains("不是最新检查点"));
        assertEquals(ExecutionStatus.PAUSED,
                runtime.getStatus(ACTOR, "checkpoint-drift"));
        verify(repository, never()).update(any(TenantId.class), any(AgentExecutionBO.class));
    }

    @Test
    void executionRecordTracksTheLastDurableGraphCheckpoint() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AtomicReference<AgentExecutionBO> stored = new AtomicReference<>();
        List<String> checkpointIds = new CopyOnWriteArrayList<>();
        when(repository.selectByExecutionId(eq(ACTOR.tenantId()), anyString()))
                .thenAnswer(invocation -> stored.get());
        doAnswer(invocation -> {
            stored.set(invocation.getArgument(1));
            return null;
        }).when(repository).insert(eq(ACTOR.tenantId()), any(AgentExecutionBO.class));
        doAnswer(invocation -> {
            stored.set(invocation.getArgument(1));
            return null;
        }).when(repository).update(eq(ACTOR.tenantId()), any(AgentExecutionBO.class));
        doAnswer(invocation -> {
            AgentCheckpointBO checkpoint = invocation.getArgument(1);
            checkpointIds.add(checkpoint.getCheckpointId());
            return null;
        }).when(checkpoints).insert(eq(ACTOR.tenantId()), any(AgentCheckpointBO.class));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        Execution result = runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"));

        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertTrue(checkpointIds.size() >= 2);
        assertEquals(checkpointIds.getLast(), stored.get().getLastCheckpointId());
    }

    @Test
    void failedResumeWriteDoesNotExposeUnpersistedRunningExecution() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO paused = persisted("resume-write-failed", 3, null, null);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "resume-write-failed"))
                .thenReturn(paused);
        AgentCheckpointBO finalCheckpoint = new AgentCheckpointBO();
        finalCheckpoint.setTenantId(9L);
        finalCheckpoint.setExecutionId("resume-write-failed");
        finalCheckpoint.setNodeId("graph_final");
        finalCheckpoint.setStateData("{\"answer\":\"saved\"}");
        when(checkpoints.selectLatestByExecutionId(ACTOR.tenantId(), "resume-write-failed"))
                .thenReturn(finalCheckpoint);
        doThrow(new IllegalStateException("write unavailable"))
                .when(repository).update(eq(ACTOR.tenantId()),
                        argThat(value -> "resume-write-failed".equals(value.getExecutionId())
                                && Integer.valueOf(0).equals(value.getStatus())));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, "resume-write-failed"));

        assertEquals(ExecutionStatus.PAUSED,
                runtime.getStatus(ACTOR, "resume-write-failed"));
    }

    @Test
    void missingDefinitionDuringCheckpointResumeKeepsPersistedExecutionPaused() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO paused = persisted("missing-definition", 3, null, null);
        paused.setAgentId("removed-agent");
        when(repository.selectByExecutionId(ACTOR.tenantId(), "missing-definition"))
                .thenReturn(paused);
        AgentCheckpointBO checkpoint = new AgentCheckpointBO();
        checkpoint.setTenantId(9L);
        checkpoint.setExecutionId("missing-definition");
        checkpoint.setNodeId("graph_final");
        checkpoint.setStateData("{\"answer\":\"saved\"}");
        when(checkpoints.selectLatestByExecutionId(ACTOR.tenantId(), "missing-definition"))
                .thenReturn(checkpoint);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, "missing-definition"));
        assertEquals(ExecutionStatus.PAUSED,
                runtime.getStatus(ACTOR, "missing-definition"));
        verify(repository, never()).update(any(TenantId.class), any(AgentExecutionBO.class));
    }

    @Test
    void persistedPauseWithIntermediateCheckpointKeepsPausedStatus() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO paused = persisted("tool-done", 3, null, null);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "tool-done"))
                .thenReturn(paused);
        AgentCheckpointBO partial = new AgentCheckpointBO();
        partial.setTenantId(9L);
        partial.setExecutionId("tool-done");
        partial.setNodeId("tool_done");
        partial.setStateData("{\"step\":1}");
        when(checkpoints.selectLatestByExecutionId(ACTOR.tenantId(), "tool-done"))
                .thenReturn(partial);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        IllegalStateException rejected = assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, "tool-done"));

        assertTrue(rejected.getMessage().contains("旧格式中间检查点缺少下一节点"));
        assertEquals(ExecutionStatus.PAUSED, runtime.getStatus(ACTOR, "tool-done"));
        verify(repository, never()).update(any(TenantId.class), any(AgentExecutionBO.class));
    }

    @Test
    void persistedStartedMarkerKeepsPauseAndNeverReplaysTheNode() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO paused = persisted("started-node", 3, null, null);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "started-node"))
                .thenReturn(paused);
        AgentCheckpointBO marker = new AgentCheckpointBO();
        marker.setTenantId(9L);
        marker.setCheckpointId("cp-started");
        marker.setExecutionId("started-node");
        marker.setNodeId("__agent_node_started__");
        marker.setStateData("{\"__agentCheckpointFormat\":\"langgraph4j-v1\","
                + "\"nextNodeId\":\"block\",\"state\":{\"step\":1}}");
        when(checkpoints.selectLatestByExecutionId(ACTOR.tenantId(), "started-node"))
                .thenReturn(marker);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        IllegalStateException rejected = assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, "started-node"));

        assertTrue(rejected.getMessage().contains("拒绝重复副作用"));
        assertEquals(ExecutionStatus.PAUSED, runtime.getStatus(ACTOR, "started-node"));
        verify(repository, never()).update(any(TenantId.class), any(AgentExecutionBO.class));
    }

    @Test
    void persistedNonresumableStateKeepsPauseAndNeverReplaysTheNode() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentCheckpointRepository checkpoints = mock(AgentCheckpointRepository.class);
        AgentExecutionBO paused = persisted("typed-state", 3, null, null);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "typed-state"))
                .thenReturn(paused);
        AgentCheckpointBO marker = new AgentCheckpointBO();
        marker.setTenantId(9L);
        marker.setCheckpointId("cp-nonresumable");
        marker.setExecutionId("typed-state");
        marker.setNodeId("__agent_nonresumable__");
        marker.setStateData("{\"__agentCheckpointFormat\":\"langgraph4j-v2\","
                + "\"nextNodeId\":\"block\",\"state\":{},\"typeHints\":{}}");
        when(checkpoints.selectLatestByExecutionId(ACTOR.tenantId(), "typed-state"))
                .thenReturn(marker);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(),
                mock(EventPublisher.class), null, checkpoints);

        IllegalStateException rejected = assertThrows(IllegalStateException.class,
                () -> runtime.resume(ACTOR, "typed-state"));

        assertTrue(rejected.getMessage().contains("拒绝自动续跑"));
        assertEquals(ExecutionStatus.PAUSED, runtime.getStatus(ACTOR, "typed-state"));
        verify(repository, never()).update(any(TenantId.class), any(AgentExecutionBO.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void coversExecutionStatusMappingAndRuntimeInputHelpers() throws Exception {
        assertNull(AgentRuntimeImpl.toStoredStatus(null));
        assertEquals(0, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.PENDING));
        assertEquals(0, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.RUNNING));
        assertEquals(3, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.PAUSED));
        assertEquals(1, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.COMPLETED));
        assertEquals(2, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.FAILED));
        assertEquals(4, AgentRuntimeImpl.toStoredStatus(ExecutionStatus.CANCELLED));
        assertNull(AgentRuntimeImpl.fromStoredStatus(null));
        assertNull(AgentRuntimeImpl.fromStoredStatus(99));
        assertEquals(ExecutionStatus.RUNNING, AgentRuntimeImpl.fromStoredStatus(0));
        assertEquals(ExecutionStatus.COMPLETED, AgentRuntimeImpl.fromStoredStatus(1));
        assertEquals(ExecutionStatus.FAILED, AgentRuntimeImpl.fromStoredStatus(2));
        assertEquals(ExecutionStatus.PAUSED, AgentRuntimeImpl.fromStoredStatus(3));
        assertEquals(ExecutionStatus.CANCELLED, AgentRuntimeImpl.fromStoredStatus(4));

        Method withActor =
                AgentRuntimeImpl.class.getDeclaredMethod(
                        "withActor", ActorContext.class, Map.class);
        withActor.setAccessible(true);
        Map<String, Object> actorInput = (Map<String, Object>) withActor.invoke(null, ACTOR, null);
        assertEquals(9L, actorInput.get("tenantId"));
        assertEquals(7L, actorInput.get("userId"));
        Method withVersion =
                AgentRuntimeImpl.class.getDeclaredMethod("withVersion", Map.class, String.class);
        withVersion.setAccessible(true);
        assertEquals("v1", ((Map<?, ?>) withVersion.invoke(null, null, "v1")).get("version"));
        Method withRuntime =
                AgentRuntimeImpl.class.getDeclaredMethod(
                        "withRuntime", Map.class, com.shiyu.ai.agent.contract.runtime.AiRun.class);
        withRuntime.setAccessible(true);
        Map<String, Object> input = new java.util.HashMap<>();
        assertSame(input, withRuntime.invoke(null, input, null));

        AgentRuntimeImpl runtime =
                newRuntime(mock(AgentExecutionRepository.class), new ReleasingNode());
        Method number = AgentRuntimeImpl.class.getDeclaredMethod("number", Object.class);
        number.setAccessible(true);
        assertEquals(3L, number.invoke(runtime, 3));
        assertEquals(4L, number.invoke(runtime, "4"));
        assertEquals(0L, number.invoke(runtime, "not-a-number"));
        assertEquals(0L, number.invoke(runtime, new Object[] {null}));
        Method string = AgentRuntimeImpl.class.getDeclaredMethod("string", Object.class);
        string.setAccessible(true);
        assertNull(string.invoke(runtime, new Object[] {null}));
        assertEquals("value", string.invoke(runtime, "value"));
    }

    @Test
    void coversRestoredExecutionAndRuntimeEventHelperBoundaries() throws Exception {
        AgentRuntimeImpl runtime =
                newRuntime(mock(AgentExecutionRepository.class), new ReleasingNode());
        Method resolve =
                AgentRuntimeImpl.class.getDeclaredMethod("resolveStatus", AgentExecutionBO.class);
        resolve.setAccessible(true);
        AgentExecutionBO unknown = persisted("unknown", 99, null, null);
        assertNull(resolve.invoke(null, unknown));
        AgentExecutionBO running = persisted("running", 0, null, null);
        assertEquals(ExecutionStatus.RUNNING, resolve.invoke(null, running));
        running.setEndTime(LocalDateTime.now());
        assertEquals(ExecutionStatus.COMPLETED, resolve.invoke(null, running));
        running.setErrorMessage("failure");
        assertEquals(ExecutionStatus.FAILED, resolve.invoke(null, running));

        Method parse = AgentRuntimeImpl.class.getDeclaredMethod("parseData", String.class);
        parse.setAccessible(true);
        assertNull(parse.invoke(null, new Object[] {null}));
        assertTrue(((Map<?, ?>) parse.invoke(null, "{}")).isEmpty());
        assertThrows(InvocationTargetException.class, () -> parse.invoke(null, "not-json"));

        Method withRuntime =
                AgentRuntimeImpl.class.getDeclaredMethod("withRuntime", Map.class, AiRun.class);
        withRuntime.setAccessible(true);
        Map<String, Object> input = new java.util.HashMap<>();
        AiRun run =
                new AiRun(
                        "helper-run",
                        new TenantId(9L),
                        new UserId(7L),
                        null,
                        null,
                        AiRunSource.AGENT,
                        "agent-1",
                        null,
                        "trace",
                        null,
                        null,
                        null,
                        null,
                        null,
                        AiRunStatus.RUNNING,
                        0,
                        0,
                        true,
                        null,
                        Instant.now(),
                        null,
                        null,
                        0L);
        assertSame(input, withRuntime.invoke(null, input, run));
        assertEquals("helper-run", input.get("__aiRunId"));
    }

    @Test
    void coversExecutionInputParsingLoaderMissAndTenantAccessGuard() throws Exception {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        Method create =
                AgentRuntimeImpl.class.getDeclaredMethod(
                        "createExecution",
                        ActorContext.class,
                        String.class,
                        String.class,
                        Map.class);
        create.setAccessible(true);
        Map<String, Object> input = new java.util.HashMap<>();
        input.put("userId", "not-a-number");
        input.put("sessionId", 42);
        Execution parsed = (Execution) create.invoke(runtime, ACTOR, "agent-1", null, input);
        assertNull(parsed.getUserId());
        assertEquals("42", parsed.getSessionId());
        input.put("userId", "7");
        parsed = (Execution) create.invoke(runtime, ACTOR, "agent-1", null, input);
        assertEquals(7L, parsed.getUserId());

        AgentRuntimeImpl missing = newRuntime(repository, new ReleasingNode());
        Method definition =
                AgentRuntimeImpl.class.getDeclaredMethod(
                        "getAgentDefinition", ActorContext.class, String.class);
        definition.setAccessible(true);
        InvocationTargetException loaderMiss =
                assertThrows(
                        InvocationTargetException.class,
                        () -> definition.invoke(missing, ACTOR, "missing-agent"));
        assertTrue(loaderMiss.getCause() instanceof IllegalStateException);

        AgentExecutionBO unscoped = persisted("unscoped", 0, null, null);
        unscoped.setTenantId(null);
        unscoped.setInputData("{}");
        when(repository.selectByExecutionId(new TenantId(9), "unscoped")).thenReturn(unscoped);
        assertThrows(IllegalStateException.class, () -> runtime.getStatus(ACTOR, "unscoped"));
    }

    @Test
    void enforcesRuntimeAdmissionAndTerminalEventBoundaries() throws Exception {
        AiRun runtimeRun =
                new AiRun(
                        "runtime-run",
                        new TenantId(9L),
                        new UserId(7L),
                        null,
                        null,
                        AiRunSource.AGENT,
                        "agent-1",
                        null,
                        "trace",
                        null,
                        null,
                        "execution",
                        null,
                        null,
                        AiRunStatus.RUNNING,
                        0,
                        0,
                        true,
                        null,
                        Instant.now(),
                        null,
                        null,
                        0L);
        AiRuntimeService aiRuntime = mock(AiRuntimeService.class);
        when(aiRuntime.startRun(
                        any(AiRunContext.class),
                        eq(AiRunSource.AGENT),
                        anyString(),
                        isNull(),
                        anyString()))
                .thenReturn(runtimeRun);
        AgentRuntimeImpl runtime =
                newRuntime(
                        mock(AgentExecutionRepository.class),
                        new ReleasingNode(),
                        mock(EventPublisher.class),
                        aiRuntime);

        Method start =
                AgentRuntimeImpl.class.getDeclaredMethod(
                        "startRuntime", Execution.class, Map.class);
        start.setAccessible(true);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        Map<String, Object> input = new java.util.HashMap<>(Map.of("tenantId", 9L, "userId", 7L));
        assertSame(runtimeRun, start.invoke(runtime, execution, input));
        verify(aiRuntime).append(runtimeRun, AiRunEventType.MODEL_STARTED, "{}", true);

        assertNull(start.invoke(runtime, execution, Map.of("tenantId", 0L, "userId", 7L)));
        when(aiRuntime.startRun(
                        any(AiRunContext.class),
                        eq(AiRunSource.AGENT),
                        anyString(),
                        isNull(),
                        anyString()))
                .thenThrow(new IllegalStateException("runtime down"));
        assertNull(start.invoke(runtime, execution, Map.of("tenantId", 9L, "userId", 7L)));
        InvocationTargetException admission =
                assertThrows(
                        InvocationTargetException.class,
                        () ->
                                start.invoke(
                                        runtime,
                                        execution,
                                        Map.of("tenantId", 9L, "userId", 7L, "__appId", "app-1")));
        assertTrue(admission.getCause() instanceof IllegalStateException);

        Method append =
                AgentRuntimeImpl.class.getDeclaredMethod(
                        "appendRuntime", AiRun.class, AiRunEventType.class, String.class);
        append.setAccessible(true);
        append.invoke(runtime, new Object[] {null, AiRunEventType.MODEL_DELTA, null});
        when(aiRuntime.startRun(
                        any(AiRunContext.class),
                        eq(AiRunSource.AGENT),
                        anyString(),
                        isNull(),
                        anyString()))
                .thenReturn(runtimeRun);
        append.invoke(runtime, runtimeRun, AiRunEventType.MODEL_DELTA, null);
        verify(aiRuntime).append(runtimeRun, AiRunEventType.MODEL_DELTA, null, true);

        Method finish =
                AgentRuntimeImpl.class.getDeclaredMethod(
                        "finishRuntime", AiRun.class, Execution.class);
        finish.setAccessible(true);
        Execution completed = mock(Execution.class);
        when(completed.getStatus()).thenReturn(ExecutionStatus.COMPLETED);
        when(completed.getErrorMessage()).thenReturn(null);
        finish.invoke(runtime, runtimeRun, completed);
        verify(aiRuntime)
                .finish(
                        "runtime-run",
                        new com.shiyu.ai.kernel.context.TenantId(9L),
                        7L,
                        AiRunStatus.COMPLETED,
                        null);
        Execution cancelled = mock(Execution.class);
        when(cancelled.getStatus()).thenReturn(ExecutionStatus.CANCELLED);
        finish.invoke(runtime, runtimeRun, cancelled);
        verify(aiRuntime)
                .finish(
                        "runtime-run",
                        new com.shiyu.ai.kernel.context.TenantId(9L),
                        7L,
                        AiRunStatus.CANCELLED,
                        null);
        Execution failed = mock(Execution.class);
        when(failed.getStatus()).thenReturn(ExecutionStatus.FAILED);
        when(failed.getErrorMessage()).thenReturn("boom");
        finish.invoke(runtime, runtimeRun, failed);
        verify(aiRuntime)
                .finish(
                        "runtime-run",
                        new com.shiyu.ai.kernel.context.TenantId(9L),
                        7L,
                        AiRunStatus.FAILED,
                        "AGENT_EXECUTION_FAILED");
        doThrow(new IllegalStateException("late failure"))
                .when(aiRuntime)
                .finish(
                        "runtime-run",
                        new com.shiyu.ai.kernel.context.TenantId(9L),
                        7L,
                        AiRunStatus.FAILED,
                        "AGENT_EXECUTION_FAILED");
        finish.invoke(runtime, runtimeRun, failed);
    }

    private static AgentExecutionBO persisted(
            String id, int status, LocalDateTime endTime, String error) {
        AgentExecutionBO bo = new AgentExecutionBO();
        bo.setExecutionId(id);
        bo.setAgentId("agent-1");
        bo.setVersion("v1");
        bo.setTenantId(9L);
        bo.setUserId(7L);
        bo.setSessionId("7");
        bo.setStatus(status);
        bo.setErrorMessage(error);
        bo.setInputData("{\"tenantId\":9,\"userId\":7}");
        bo.setOutputData("{}");
        bo.setStartTime(LocalDateTime.now().minusSeconds(1));
        bo.setEndTime(endTime);
        bo.setDurationMs(1L);
        return bo;
    }

    private static AgentRuntimeImpl newRuntime(
            AgentExecutionRepository repository, BlockingNode node) {
        return newRuntime(repository, node, mock(EventPublisher.class));
    }

    private static AgentRuntimeImpl newRuntime(AgentExecutionRepository repository, BaseNode node) {
        return newRuntime(repository, node, mock(EventPublisher.class));
    }

    private static AgentRuntimeImpl newRuntime(
            AgentExecutionRepository repository, BaseNode node, EventPublisher publisher) {
        return newRuntime(repository, node, publisher, null);
    }

    private static AgentRuntimeImpl newRuntime(
            AgentExecutionRepository repository,
            BaseNode node,
            EventPublisher publisher,
            AiRuntimeService runtime) {
        return newRuntime(repository, node, publisher, runtime,
                mock(AgentCheckpointRepository.class));
    }

    @Test
    void blankVersionPersistsTheVersionThatActuallyRan() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        Execution result = runtime.execute(ACTOR, "agent-1", " ", Map.of("message", "hello"));

        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals("v1", result.getVersion());
        verify(repository, atLeastOnce()).insert(eq(ACTOR.tenantId()),
                argThat(record -> "v1".equals(record.getVersion())));
    }

    @Test
    void missingVersionIsRejectedBeforeAnExecutionIsPersisted() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        assertThrows(IllegalArgumentException.class,
                () -> runtime.execute(ACTOR, "agent-1", "missing", Map.of()));
        verify(repository, never()).insert(any(TenantId.class), any(AgentExecutionBO.class));
    }

    @Test
    void completedEventFailureStillRemovesTerminalExecutionFromMemory() throws Exception {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AtomicReference<String> executionId = new AtomicReference<>();
        doAnswer(invocation -> {
            AgentExecutionBO saved = invocation.getArgument(1);
            executionId.set(saved.getExecutionId());
            return null;
        }).when(repository).insert(any(TenantId.class), any(AgentExecutionBO.class));
        EventPublisher publisher = mock(EventPublisher.class);
        IllegalStateException eventFailure = new IllegalStateException("completion event unavailable");
        doThrow(eventFailure).when(publisher).publish(any(AgentExecutionCompletedEvent.class));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        assertSame(eventFailure, assertThrows(IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"))));
        java.lang.reflect.Field stateStoreField =
                AgentRuntimeImpl.class.getDeclaredField("stateStore");
        stateStoreField.setAccessible(true);
        var stateStore = (com.shiyu.ai.agent.implementation.runtime.port.AgentRuntimeStateStore)
                stateStoreField.get(runtime);
        assertNull(stateStore.active(executionId.get()));
    }

    @Test
    void failedStateWriteFailureStillRemovesTerminalExecutionFromMemory() throws Exception {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AtomicReference<String> executionId = new AtomicReference<>();
        IllegalStateException writeFailure = new IllegalStateException("failed state unavailable");
        doAnswer(invocation -> {
            AgentExecutionBO saved = invocation.getArgument(1);
            executionId.set(saved.getExecutionId());
            if (Integer.valueOf(2).equals(saved.getStatus())) throw writeFailure;
            return null;
        }).when(repository).insert(any(TenantId.class), any(AgentExecutionBO.class));
        AgentRuntimeImpl runtime = newRuntime(repository, new FailingNode());

        assertSame(writeFailure, assertThrows(IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"))));
        java.lang.reflect.Field stateStoreField =
                AgentRuntimeImpl.class.getDeclaredField("stateStore");
        stateStoreField.setAccessible(true);
        var stateStore = (com.shiyu.ai.agent.implementation.runtime.port.AgentRuntimeStateStore)
                stateStoreField.get(runtime);
        assertNull(stateStore.active(executionId.get()));
    }

    @Test
    void aStreamingExecutionCannotReplayItsNodesOnSecondSubscription() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        BaseNode node = new BaseNode(NodeConfig.builder().nodeId("block").nodeName("block")
                .nodeType(NodeType.DEFAULT).timeout(0L).build()) {
            @Override
            public NodeOutput doExecute(NodeInput input) {
                calls.incrementAndGet();
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                return output;
            }

            @Override
            public List<NodeInputParam> getRequiredInputs() {
                return List.of();
            }
        };
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), node);
        reactor.core.publisher.Flux<Map<String, Object>> stream =
                runtime.executeStream(ACTOR, "agent-1", Map.of());

        assertTrue(stream.collectList().block(java.time.Duration.ofSeconds(5)).stream()
                .anyMatch(event -> "COMPLETED".equals(event.get("status"))));
        assertThrows(IllegalStateException.class,
                () -> stream.collectList().block(java.time.Duration.ofSeconds(5)));
        assertEquals(1, calls.get());
    }

    private static AgentRuntimeImpl newRuntime(
            AgentExecutionRepository repository,
            BaseNode node,
            EventPublisher publisher,
            AiRuntimeService runtime,
            AgentCheckpointRepository checkpointRepository) {
        return newRuntime(repository, node, publisher, runtime, checkpointRepository, null);
    }

    private static AgentRuntimeImpl newRuntime(
            AgentExecutionRepository repository,
            BaseNode node,
            EventPublisher publisher,
            AiRuntimeService runtime,
            AgentCheckpointRepository checkpointRepository,
            ToolApprovalService approvals) {
        AgentAdminRepository adminRepository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cacheManager = new AgentCacheManager(adminRepository, loader);

        Graph graph =
                Graph.builder()
                        .name("control-test")
                        .startNode("block")
                        .endNode("block")
                        .nodes(Map.of("block", node))
                        .edges(Map.of())
                        .conditionalEdges(Map.of())
                        .build();
        AgentVersion version =
                AgentVersion.builder()
                        .versionNumber("v1")
                        .description("runtime control test")
                        .graph(graph)
                        .build();
        AgentDefinition definition =
                AgentDefinition.builder()
                        .agentId("agent-1")
                        .name("control-test")
                        .currentVersion("v1")
                        .build();
        definition.addVersion(version);
        cacheManager.putSystem(definition);

        return new AgentRuntimeImpl(
                cacheManager, loader, repository, checkpointRepository, publisher, runtime,
                approvals);
    }

    /**
     * 验证 Execution Recorder 相关功能、边界条件、异常路径和协作行为。
     */
    private static final class ExecutionRecorder {

        private final List<AgentExecutionBO> stored = new CopyOnWriteArrayList<>();
        private final CountDownLatch inserted = new CountDownLatch(1);

        private ExecutionRecorder(AgentExecutionRepository repository) {
            doAnswer(
                            invocation -> {
                                stored.add(invocation.getArgument(1));
                                inserted.countDown();
                                return null;
                            })
                    .when(repository)
                    .insert(any(TenantId.class), any(AgentExecutionBO.class));
            when(repository.selectByExecutionId(any(TenantId.class), anyString()))
                    .thenAnswer(
                            invocation -> {
                                String executionId = invocation.getArgument(1);
                                return stored.stream()
                                        .filter(bo -> executionId.equals(bo.getExecutionId()))
                                        .findFirst()
                                        .orElse(null);
                            });
        }

        private String awaitExecutionId() throws Exception {
            assertTrue(
                    inserted.await(5, TimeUnit.SECONDS), "execution should be persisted on start");
            return stored.getFirst().getExecutionId();
        }
    }

    /**
     * 验证 Blocking 相关功能、边界条件、异常路径和协作行为。
     */
    private static final class BlockingNode extends BaseNode {

        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);

        private BlockingNode() {
            super(
                    NodeConfig.builder()
                            .nodeId("block")
                            .nodeName("block")
                            .nodeType(NodeType.DEFAULT)
                            .timeout(0L)
                            .build());
        }

        @Override
        public NodeOutput doExecute(NodeInput input) throws Exception {
            entered.countDown();
            release.await();
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.setMsg("blocked node released");
            return output;
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() {
            return List.of();
        }
    }

    /**
     * 验证 Releasing 相关功能、边界条件、异常路径和协作行为。
     */
    private static final class ReleasingNode extends BaseNode {
        private ReleasingNode() {
            super(
                    NodeConfig.builder()
                            .nodeId("block")
                            .nodeName("block")
                            .nodeType(NodeType.DEFAULT)
                            .timeout(0L)
                            .build());
        }

        @Override
        public NodeOutput doExecute(NodeInput input) {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.addData("message", "ok");
            return output;
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() {
            return List.of();
        }
    }

    /** 模拟提供方将失败作为返回值而非异常传出的节点。 */
    private static final class FailedOutputNode extends BaseNode {
        private FailedOutputNode() {
            super(NodeConfig.builder().nodeId("block").nodeName("block")
                    .nodeType(NodeType.DEFAULT).timeout(0L).build());
        }

        @Override
        public NodeOutput doExecute(NodeInput input) {
            NodeOutput output = new NodeOutput();
            output.setSuccess(false);
            output.setMsg("model call failed");
            return output;
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() {
            return List.of();
        }
    }

    /**
     * 验证 Failing 相关功能、边界条件、异常路径和协作行为。
     */
    private static final class FailingNode extends BaseNode {
        private FailingNode() {
            super(
                    NodeConfig.builder()
                            .nodeId("block")
                            .nodeName("block")
                            .nodeType(NodeType.DEFAULT)
                            .timeout(0L)
                            .build());
        }

        @Override
        public NodeOutput doExecute(NodeInput input) {
            throw new IllegalStateException("node failed");
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() {
            return List.of();
        }
    }
}

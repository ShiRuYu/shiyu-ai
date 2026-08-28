package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.event.EventPublisher;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.agent.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.kernel.error.DomainAccessDeniedException;
import com.shiyu.ai.runtime.AiRun;
import com.shiyu.ai.runtime.AiRunContext;
import com.shiyu.ai.runtime.AiRunEventType;
import com.shiyu.ai.runtime.AiRuntimeService;
import com.shiyu.ai.runtime.AiRunSource;
import com.shiyu.ai.runtime.AiRunStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.time.LocalDateTime;
import java.time.Instant;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class AgentRuntimeImplControlTest {

    private static final ActorContext ACTOR = new ActorContext(
            new TenantId(9), new UserId(7), false);

    private Disposable disposable;

    @AfterEach
    void cleanup() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Test
    void pauseThenResumeLetsStreamCompleteAndPersistsSuccess() throws Exception {
        BlockingNode node = new BlockingNode();
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        AgentRuntimeImpl runtime = newRuntime(repository, node);
        CountDownLatch terminal = new CountDownLatch(1);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();

        disposable = runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello")).subscribe(
                events::add, error -> terminal.countDown(), terminal::countDown);

        assertTrue(node.entered.await(5, TimeUnit.SECONDS), "graph node should start");
        String executionId = recorder.awaitExecutionId();

        runtime.pause(ACTOR, executionId);
        assertEquals(ExecutionStatus.PAUSED, runtime.getStatus(ACTOR, executionId));

        runtime.resume(ACTOR, executionId);
        assertEquals(ExecutionStatus.RUNNING, runtime.getStatus(ACTOR, executionId));

        node.release.countDown();
        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should complete after resume");
        assertTrue(events.stream().anyMatch(event -> "COMPLETED".equals(event.get("status"))),
                "stream should emit a COMPLETED terminal event");
        verify(repository, timeout(5000).atLeastOnce()).update(any(TenantId.class), argThat(bo ->
                executionId.equals(bo.getExecutionId()) && Integer.valueOf(1).equals(bo.getStatus())));
    }

    @Test
    void cancelDuringExecutionEmitsCancelledTerminalAndPersistsCancelled() throws Exception {
        BlockingNode node = new BlockingNode();
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionRecorder recorder = new ExecutionRecorder(repository);
        AgentRuntimeImpl runtime = newRuntime(repository, node);
        CountDownLatch terminal = new CountDownLatch(1);
        List<Map<String, Object>> events = new CopyOnWriteArrayList<>();

        disposable = runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello")).subscribe(
                events::add, error -> terminal.countDown(), terminal::countDown);

        assertTrue(node.entered.await(5, TimeUnit.SECONDS), "graph node should start");
        String executionId = recorder.awaitExecutionId();

        runtime.cancel(ACTOR, executionId);
        assertEquals(ExecutionStatus.CANCELLED, runtime.getStatus(ACTOR, executionId));

        node.release.countDown();
        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should terminate after cancel");
        assertTrue(events.stream().anyMatch(event -> "CANCELLED".equals(event.get("status"))),
                "stream should emit a CANCELLED terminal event");
        verify(repository, timeout(5000).atLeastOnce()).update(any(TenantId.class), argThat(bo ->
                executionId.equals(bo.getExecutionId()) && Integer.valueOf(4).equals(bo.getStatus())));
    }

    @Test
    void persistenceFailureFailsTheExecutionCommand() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        doThrow(new IllegalStateException("database unavailable"))
                .when(repository).insert(any(TenantId.class), any(AgentExecutionBO.class));
        AgentRuntimeImpl runtime = newRuntime(repository, new BlockingNode());

        assertThrows(IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("tenantId", 999, "userId", 999)));
    }

    @Test
    void rethrowsPersistenceFailureAfterExecutionHasStarted() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        java.util.concurrent.atomic.AtomicInteger writes = new java.util.concurrent.atomic.AtomicInteger();
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);
        doAnswer(invocation -> {
            if (writes.incrementAndGet() > 1) {
                throw new IllegalStateException("database unavailable after start");
            }
            return null;
        }).when(repository).insert(any(TenantId.class), any(AgentExecutionBO.class));
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        assertThrows(IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("message", "hello")));
        // A persistence failure while recording the terminal state must escape the
        // command; it must not be converted into a successful execution or hidden
        // behind a best-effort failure event.
        verify(publisher).publish(any(com.shiyu.ai.agent.event.AgentExecutionStartedEvent.class));
        verify(publisher, never()).publish(any(com.shiyu.ai.agent.event.AgentExecutionFailedEvent.class));
    }

    @Test
    void publishesFailureAndCleansUpWhenTerminalPersistenceFailsOnce() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        java.util.concurrent.atomic.AtomicInteger writes = new java.util.concurrent.atomic.AtomicInteger();
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);
        doAnswer(invocation -> {
            if (writes.incrementAndGet() == 2) {
                throw new IllegalStateException("terminal write failed");
            }
            return null;
        }).when(repository).insert(any(TenantId.class), any(AgentExecutionBO.class));
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode(), publisher);

        assertThrows(IllegalStateException.class,
                () -> runtime.execute(ACTOR, "agent-1", Map.of("message", "hello")));
        verify(publisher).publish(any(com.shiyu.ai.agent.event.AgentExecutionStartedEvent.class));
        verify(publisher).publish(any(com.shiyu.ai.agent.event.AgentExecutionFailedEvent.class));
    }

    @Test
    void usesDatabaseLoaderWhenScopedAndSystemCachesMiss() {
        AgentAdminRepository adminRepository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cacheManager = new AgentCacheManager(adminRepository, loader);
        ReleasingNode node = new ReleasingNode();
        Graph graph = Graph.builder().name("loader-test").startNode("block").endNode("block")
                .nodes(Map.of("block", node)).edges(Map.of()).conditionalEdges(Map.of()).build();
        AgentVersion version = AgentVersion.builder().versionNumber("v1").graph(graph).build();
        AgentDefinition definition = AgentDefinition.builder().agentId("agent-1")
                .currentVersion("v1").build();
        definition.addVersion(version);
        when(loader.loadFromDb(ACTOR, "agent-1")).thenReturn(definition);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenReturn(null);

        AgentRuntimeImpl runtime = new AgentRuntimeImpl(cacheManager, loader, repository,
                mock(AgentCheckpointRepository.class), mock(EventPublisher.class));
        assertEquals(ExecutionStatus.COMPLETED,
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

        Execution result = runtime.execute(ACTOR, "agent-1", Map.of("message", "hello", "sessionId", "s-1"));

        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals("s-1", result.getSessionId());
        assertTrue(result.getOutput() != null);
        verify(publisher).publish(any(com.shiyu.ai.agent.event.AgentExecutionStartedEvent.class));
        verify(publisher).publish(any(com.shiyu.ai.agent.event.AgentExecutionCompletedEvent.class));
        assertEquals(result.getExecutionId(), recorder.stored.getFirst().getExecutionId());
    }

    @Test
    void synchronousGraphFailureReturnsFailedExecutionAndPublishesFailure() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new FailingNode(), publisher);

        Execution result = runtime.execute(ACTOR, "agent-1", Map.of("message", "hello"));

        assertEquals(ExecutionStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("执行异常"));
        verify(publisher).publish(any(com.shiyu.ai.agent.event.AgentExecutionFailedEvent.class));
    }

    @Test
    void streamFailurePublishesFailureAndPropagatesTheOriginalError() throws Exception {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        EventPublisher publisher = mock(EventPublisher.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new FailingNode(), publisher);
        CountDownLatch terminal = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        disposable = runtime.executeStream(ACTOR, "agent-1", Map.of("message", "hello")).subscribe(
                ignored -> { }, error -> { failure.set(error); terminal.countDown(); }, terminal::countDown);

        assertTrue(terminal.await(5, TimeUnit.SECONDS), "stream should terminate with an error");
        assertTrue(failure.get() != null && failure.get().getMessage() != null);
        verify(publisher, timeout(5000)).publish(any(com.shiyu.ai.agent.event.AgentExecutionFailedEvent.class));
    }

    @Test
    void historyAndUserHistoryEnforceTenantAndUserOwnership() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO bo = new AgentExecutionBO();
        bo.setExecutionId("history-1"); bo.setAgentId("agent-1"); bo.setVersion("v1");
        bo.setTenantId(9L); bo.setUserId(7L); bo.setSessionId("7"); bo.setStatus(1);
        when(repository.selectByAgentId(new TenantId(9), "agent-1", 100)).thenReturn(List.of(bo));
        when(repository.selectBySessionId(new TenantId(9), "7")).thenReturn(List.of(bo));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());

        assertEquals(1, runtime.getHistory(ACTOR, "agent-1", 100).size());
        assertEquals(1, runtime.getUserHistory(ACTOR, 7L, 10).size());
        assertThrows(IllegalStateException.class, () -> runtime.getUserHistory(ACTOR, 8L, 10));
        ActorContext admin = new ActorContext(new TenantId(9), new UserId(99), true);
        assertEquals(1, runtime.getUserHistory(admin, 7L, 10).size());
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
        assertThrows(DomainAccessDeniedException.class,
                () -> runtime.getExecution(new ActorContext(new TenantId(10), new UserId(7), false), "stored-1"));
    }

    @Test
    void resumesAndCancelsPersistedPausedExecutionsAndClampsHistoryLimit() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentExecutionBO paused = persisted("stored-2", 3, null, null);
        when(repository.selectByExecutionId(new TenantId(9), "stored-2")).thenReturn(paused);
        when(repository.selectByAgentId(new TenantId(9), "agent-1", 1)).thenReturn(List.of(paused));
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        Execution resumed = runtime.resume(ACTOR, "stored-2");
        assertEquals(ExecutionStatus.COMPLETED, resumed.getStatus());
        assertEquals(1, runtime.getHistory(ACTOR, "agent-1", 0).size());

        AgentExecutionBO cancellable = persisted("stored-3", 0, null, null);
        when(repository.selectByExecutionId(new TenantId(9), "stored-3")).thenReturn(cancellable);
        runtime.cancel(ACTOR, "stored-3");
        verify(repository).update(any(TenantId.class), argThat(value -> Integer.valueOf(4).equals(value.getStatus())));
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

        Method withActor = AgentRuntimeImpl.class.getDeclaredMethod("withActor", ActorContext.class, Map.class);
        withActor.setAccessible(true);
        Map<String, Object> actorInput = (Map<String, Object>) withActor.invoke(null, ACTOR, null);
        assertEquals(9L, actorInput.get("tenantId"));
        assertEquals(7L, actorInput.get("userId"));
        Method withVersion = AgentRuntimeImpl.class.getDeclaredMethod("withVersion", Map.class, String.class);
        withVersion.setAccessible(true);
        assertEquals("v1", ((Map<?, ?>) withVersion.invoke(null, null, "v1")).get("version"));
        Method withRuntime = AgentRuntimeImpl.class.getDeclaredMethod("withRuntime", Map.class,
                com.shiyu.ai.runtime.AiRun.class);
        withRuntime.setAccessible(true);
        Map<String, Object> input = new java.util.HashMap<>();
        assertSame(input, withRuntime.invoke(null, input, null));

        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), new ReleasingNode());
        Method number = AgentRuntimeImpl.class.getDeclaredMethod("number", Object.class);
        number.setAccessible(true);
        assertEquals(3L, number.invoke(runtime, 3));
        assertEquals(4L, number.invoke(runtime, "4"));
        assertEquals(0L, number.invoke(runtime, "not-a-number"));
        assertEquals(0L, number.invoke(runtime, new Object[]{null}));
        Method string = AgentRuntimeImpl.class.getDeclaredMethod("string", Object.class);
        string.setAccessible(true);
        assertNull(string.invoke(runtime, new Object[]{null}));
        assertEquals("value", string.invoke(runtime, "value"));
    }

    @Test
    void coversRestoredExecutionAndRuntimeEventHelperBoundaries() throws Exception {
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), new ReleasingNode());
        Method resolve = AgentRuntimeImpl.class.getDeclaredMethod("resolveStatus", AgentExecutionBO.class);
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
        assertNull(parse.invoke(null, new Object[]{null}));
        assertTrue(((Map<?, ?>) parse.invoke(null, "{}")).isEmpty());
        assertThrows(InvocationTargetException.class, () -> parse.invoke(null, "not-json"));

        Method withRuntime = AgentRuntimeImpl.class.getDeclaredMethod("withRuntime", Map.class, AiRun.class);
        withRuntime.setAccessible(true);
        Map<String, Object> input = new java.util.HashMap<>();
        AiRun run = new AiRun("helper-run", new TenantId(9L), new UserId(7L), null, null, AiRunSource.AGENT, "agent-1",
                null, "trace", null, null, null, null, null, AiRunStatus.RUNNING,
                0, 0, true, null, Instant.now(), null, null, 0L);
        assertSame(input, withRuntime.invoke(null, input, run));
        assertEquals("helper-run", input.get("__aiRunId"));
    }

    @Test
    void coversExecutionInputParsingLoaderMissAndTenantAccessGuard() throws Exception {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeImpl runtime = newRuntime(repository, new ReleasingNode());
        Method create = AgentRuntimeImpl.class.getDeclaredMethod("createExecution", ActorContext.class,
                String.class, String.class, Map.class);
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
        Method definition = AgentRuntimeImpl.class.getDeclaredMethod("getAgentDefinition", ActorContext.class,
                String.class);
        definition.setAccessible(true);
        InvocationTargetException loaderMiss = assertThrows(InvocationTargetException.class,
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
        AiRun runtimeRun = new AiRun("runtime-run", new TenantId(9L), new UserId(7L), null, null, AiRunSource.AGENT,
                "agent-1", null, "trace", null, null, "execution", null, null,
                AiRunStatus.RUNNING, 0, 0, true, null, Instant.now(), null, null, 0L);
        AiRuntimeService aiRuntime = mock(AiRuntimeService.class);
        when(aiRuntime.startRun(any(AiRunContext.class), eq(AiRunSource.AGENT), anyString(),
                isNull(), anyString())).thenReturn(runtimeRun);
        AgentRuntimeImpl runtime = newRuntime(mock(AgentExecutionRepository.class), new ReleasingNode(),
                mock(EventPublisher.class), aiRuntime);

        Method start = AgentRuntimeImpl.class.getDeclaredMethod("startRuntime", Execution.class, Map.class);
        start.setAccessible(true);
        Execution execution = new Execution("agent-1", "v1", Map.of());
        Map<String, Object> input = new java.util.HashMap<>(Map.of("tenantId", 9L, "userId", 7L));
        assertSame(runtimeRun, start.invoke(runtime, execution, input));
        verify(aiRuntime).append(runtimeRun, AiRunEventType.MODEL_STARTED, "{}", true);

        assertNull(start.invoke(runtime, execution, Map.of("tenantId", 0L, "userId", 7L)));
        when(aiRuntime.startRun(any(AiRunContext.class), eq(AiRunSource.AGENT), anyString(),
                isNull(), anyString())).thenThrow(new IllegalStateException("runtime down"));
        assertNull(start.invoke(runtime, execution, Map.of("tenantId", 9L, "userId", 7L)));
        InvocationTargetException admission = assertThrows(InvocationTargetException.class,
                () -> start.invoke(runtime, execution, Map.of("tenantId", 9L, "userId", 7L, "__appId", "app-1")));
        assertTrue(admission.getCause() instanceof IllegalStateException);

        Method append = AgentRuntimeImpl.class.getDeclaredMethod("appendRuntime", AiRun.class,
                AiRunEventType.class, String.class);
        append.setAccessible(true);
        append.invoke(runtime, new Object[]{null, AiRunEventType.MODEL_DELTA, null});
        when(aiRuntime.startRun(any(AiRunContext.class), eq(AiRunSource.AGENT), anyString(),
                isNull(), anyString())).thenReturn(runtimeRun);
        append.invoke(runtime, runtimeRun, AiRunEventType.MODEL_DELTA, null);
        verify(aiRuntime).append(runtimeRun, AiRunEventType.MODEL_DELTA, null, true);

        Method finish = AgentRuntimeImpl.class.getDeclaredMethod("finishRuntime", AiRun.class, Execution.class);
        finish.setAccessible(true);
        Execution completed = mock(Execution.class);
        when(completed.getStatus()).thenReturn(ExecutionStatus.COMPLETED);
        when(completed.getErrorMessage()).thenReturn(null);
        finish.invoke(runtime, runtimeRun, completed);
        verify(aiRuntime).finish("runtime-run", new com.shiyu.ai.kernel.context.TenantId(9L), 7L, AiRunStatus.COMPLETED, null);
        Execution cancelled = mock(Execution.class);
        when(cancelled.getStatus()).thenReturn(ExecutionStatus.CANCELLED);
        finish.invoke(runtime, runtimeRun, cancelled);
        verify(aiRuntime).finish("runtime-run", new com.shiyu.ai.kernel.context.TenantId(9L), 7L, AiRunStatus.CANCELLED, null);
        Execution failed = mock(Execution.class);
        when(failed.getStatus()).thenReturn(ExecutionStatus.FAILED);
        when(failed.getErrorMessage()).thenReturn("boom");
        finish.invoke(runtime, runtimeRun, failed);
        verify(aiRuntime).finish("runtime-run", new com.shiyu.ai.kernel.context.TenantId(9L), 7L, AiRunStatus.FAILED, "boom");
        doThrow(new IllegalStateException("late failure")).when(aiRuntime)
                .finish("runtime-run", new com.shiyu.ai.kernel.context.TenantId(9L), 7L, AiRunStatus.FAILED, "boom");
        finish.invoke(runtime, runtimeRun, failed);
    }

    private static AgentExecutionBO persisted(String id, int status, LocalDateTime endTime, String error) {
        AgentExecutionBO bo = new AgentExecutionBO();
        bo.setExecutionId(id); bo.setAgentId("agent-1"); bo.setVersion("v1"); bo.setTenantId(9L);
        bo.setUserId(7L); bo.setSessionId("7"); bo.setStatus(status); bo.setErrorMessage(error);
        bo.setInputData("{\"tenantId\":9,\"userId\":7}"); bo.setOutputData("{}");
        bo.setStartTime(LocalDateTime.now().minusSeconds(1)); bo.setEndTime(endTime); bo.setDurationMs(1L);
        return bo;
    }

    private static AgentRuntimeImpl newRuntime(AgentExecutionRepository repository, BlockingNode node) {
        return newRuntime(repository, node, mock(EventPublisher.class));
    }

    private static AgentRuntimeImpl newRuntime(AgentExecutionRepository repository, BaseNode node) {
        return newRuntime(repository, node, mock(EventPublisher.class));
    }

    private static AgentRuntimeImpl newRuntime(AgentExecutionRepository repository, BaseNode node,
                                               EventPublisher publisher) {
        return newRuntime(repository, node, publisher, null);
    }

    private static AgentRuntimeImpl newRuntime(AgentExecutionRepository repository, BaseNode node,
                                               EventPublisher publisher, AiRuntimeService runtime) {
        AgentAdminRepository adminRepository = mock(AgentAdminRepository.class);
        AgentLoader loader = mock(AgentLoader.class);
        AgentCacheManager cacheManager = new AgentCacheManager(adminRepository, loader);

        Graph graph = Graph.builder()
                .name("control-test")
                .startNode("block")
                .endNode("block")
                .nodes(Map.of("block", node))
                .edges(Map.of())
                .conditionalEdges(Map.of())
                .build();
        AgentVersion version = AgentVersion.builder()
                .versionNumber("v1")
                .description("runtime control test")
                .graph(graph)
                .build();
        AgentDefinition definition = AgentDefinition.builder()
                .agentId("agent-1")
                .name("control-test")
                .currentVersion("v1")
                .build();
        definition.addVersion(version);
        cacheManager.putSystem(definition);

        AgentCheckpointRepository checkpointRepository = mock(AgentCheckpointRepository.class);
        return new AgentRuntimeImpl(cacheManager, loader, repository, checkpointRepository, publisher, runtime);
    }

    private static final class ExecutionRecorder {

        private final List<AgentExecutionBO> stored = new CopyOnWriteArrayList<>();
        private final CountDownLatch inserted = new CountDownLatch(1);

        private ExecutionRecorder(AgentExecutionRepository repository) {
            doAnswer(invocation -> {
                stored.add(invocation.getArgument(1));
                inserted.countDown();
                return null;
            }).when(repository).insert(any(TenantId.class), any(AgentExecutionBO.class));
            when(repository.selectByExecutionId(any(TenantId.class), anyString())).thenAnswer(invocation -> {
                String executionId = invocation.getArgument(1);
                return stored.stream()
                        .filter(bo -> executionId.equals(bo.getExecutionId()))
                        .findFirst()
                        .orElse(null);
            });
        }

        private String awaitExecutionId() throws Exception {
            assertTrue(inserted.await(5, TimeUnit.SECONDS), "execution should be persisted on start");
            return stored.getFirst().getExecutionId();
        }
    }

    private static final class BlockingNode extends BaseNode {

        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);

        private BlockingNode() {
            super(NodeConfig.builder()
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

    private static final class ReleasingNode extends BaseNode {
        private ReleasingNode() {
            super(NodeConfig.builder().nodeId("block").nodeName("block")
                    .nodeType(NodeType.DEFAULT).timeout(0L).build());
        }

        @Override
        public NodeOutput doExecute(NodeInput input) {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            output.addData("message", "ok");
            return output;
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() { return List.of(); }
    }

    private static final class FailingNode extends BaseNode {
        private FailingNode() {
            super(NodeConfig.builder().nodeId("block").nodeName("block")
                    .nodeType(NodeType.DEFAULT).timeout(0L).build());
        }

        @Override
        public NodeOutput doExecute(NodeInput input) {
            throw new IllegalStateException("node failed");
        }

        @Override
        public List<NodeInputParam> getRequiredInputs() { return List.of(); }
    }
}

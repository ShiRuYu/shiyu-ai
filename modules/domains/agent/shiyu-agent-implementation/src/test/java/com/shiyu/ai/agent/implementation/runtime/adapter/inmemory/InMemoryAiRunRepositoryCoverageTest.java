package com.shiyu.ai.agent.implementation.runtime.adapter.inmemory;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiRunRepository;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class InMemoryAiRunRepositoryCoverageTest {
    private static final Instant CREATED_AT = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void enforcesTenantScopeVersionAndTerminalEventIdempotency() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, "c", "g", null, null, Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");

        assertTrue(repository.find(run.id(), new TenantId(1), 2).isPresent());
        assertTrue(repository.find(run.id(), new TenantId(9), 2).isEmpty());
        assertEquals(1, repository.list(new TenantId(1), 2, 0).size());
        assertEquals(0, repository.findByGeneration("g", new TenantId(9), 2).stream().count());
        assertEquals(1, repository.findByExecution(null, new TenantId(1), 2).stream().count());
        assertEquals(0, repository.update(run, -1));
        assertEquals(
                0,
                repository.update(run, run.version() - 1),
                "a stale CAS must fail even when the caller retries the same instance");
        assertThrows(IllegalStateException.class, () -> repository.insert(run));

        runtime.finish(run.id(), new TenantId(1), 2, AiRunStatus.COMPLETED, "done");
        long terminalSeq = runtime.events(run.id(), new TenantId(1), 2, 0, 10).getLast().seq();
        assertEquals(
                terminalSeq,
                repository.appendNextEvent(
                        run.id(),
                        new TenantId(1),
                        2,
                        AiRunEventType.RUN_COMPLETED,
                        "{\"errorCode\":\"done\"}",
                        true,
                        Instant.now()));
        assertThrows(
                IllegalStateException.class,
                () ->
                        repository.appendNextEvent(
                                run.id(),
                                new TenantId(1),
                                2,
                                AiRunEventType.RUN_FAILED,
                                "{}",
                                true,
                                Instant.now()));
        assertThrows(
                IllegalStateException.class,
                () ->
                        repository.appendNextEvent(
                                run.id(),
                                new TenantId(1),
                                2,
                                AiRunEventType.RUN_COMPLETED,
                                "different",
                                true,
                                Instant.now()));
    }

    @Test
    void rejectsNonContiguousExternalEventsAndAcceptsIdempotentReplay() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");
        AiRunEvent event =
                new AiRunEvent(
                        run.id(),
                        new TenantId(1),
                        3,
                        AiRunEventType.MODEL_DELTA,
                        "{}",
                        true,
                        Instant.now());
        assertThrows(IllegalStateException.class, () -> repository.appendEvent(event));
        AiRunEvent first =
                new AiRunEvent(
                        run.id(),
                        new TenantId(1),
                        2,
                        AiRunEventType.MODEL_DELTA,
                        "{}",
                        true,
                        Instant.now());
        assertEquals(2, repository.appendEvent(first));
        assertEquals(2, repository.appendEvent(first));
        assertThrows(
                IllegalStateException.class,
                () ->
                        repository.appendEvent(
                                new AiRunEvent(
                                        run.id(),
                                        new TenantId(1),
                                        2,
                                        AiRunEventType.MODEL_DELTA,
                                        "other",
                                        true,
                                        Instant.now())));
    }

    @Test
    void treatsNullTerminalPayloadAsTheCanonicalEmptyPayloadOnReplay() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");

        long sequence =
                repository.appendNextEvent(
                        run.id(),
                        new TenantId(1),
                        2,
                        AiRunEventType.RUN_COMPLETED,
                        null,
                        false,
                        Instant.now());

        assertEquals(
                sequence,
                repository.appendNextEvent(
                        run.id(),
                        new TenantId(1),
                        2,
                        AiRunEventType.RUN_COMPLETED,
                        null,
                        false,
                        Instant.now()));
    }

    @Test
    void externalEventsAdvanceRunSequenceAndCannotFollowTerminalState() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");

        AiRunEvent delta =
                new AiRunEvent(
                        run.id(),
                        new TenantId(1),
                        2,
                        AiRunEventType.MODEL_DELTA,
                        "{}",
                        true,
                        Instant.now());
        assertEquals(2, repository.appendEvent(delta));
        assertEquals(2, repository.find(run.id(), new TenantId(1), 2).orElseThrow().lastEventSeq());

        AiRun completed =
                runtime.finish(run.id(), new TenantId(1), 2, AiRunStatus.COMPLETED, "done");
        assertEquals(3, completed.lastEventSeq());
        assertThrows(
                IllegalStateException.class,
                () ->
                        repository.appendEvent(
                                new AiRunEvent(
                                        run.id(),
                                        new TenantId(1),
                                        4,
                                        AiRunEventType.MODEL_DELTA,
                                        "{}",
                                        true,
                                        Instant.now())));
    }

    @Test
    void rollsBackTerminalStateWhenEventAppendFails() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");
        repository.appendNextEvent(
                run.id(),
                new TenantId(1),
                2,
                AiRunEventType.RUN_FAILED,
                "existing",
                true,
                Instant.now());

        assertThrows(
                IllegalStateException.class,
                () ->
                        repository.updateTerminalAndAppend(
                                run.transition(AiRunStatus.COMPLETED),
                                run.version(),
                                AiRunEventType.RUN_COMPLETED,
                                "different",
                                true));
        assertEquals(
                AiRunStatus.RUNNING,
                repository.find(run.id(), new TenantId(1), 2).orElseThrow().status());
    }

    @Test
    void readsEventStreamSafelyWhileEventsAreAppended() throws Exception {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");
        for (int i = 0; i < 10_000; i++) {
            repository.appendNextEvent(
                    run.id(),
                    new TenantId(1),
                    2,
                    AiRunEventType.MODEL_DELTA,
                    "{}",
                    true,
                    Instant.now());
        }

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<?> writer =
                pool.submit(
                        () -> {
                            start.await();
                            for (int i = 0; i < 100; i++) {
                                repository.appendNextEvent(
                                        run.id(),
                                        new TenantId(1),
                                        2,
                                        AiRunEventType.MODEL_DELTA,
                                        "{}",
                                        true,
                                        Instant.now());
                            }
                            return null;
                        });
        Future<?> reader =
                pool.submit(
                        () -> {
                            start.await();
                            for (int i = 0; i < 100; i++)
                                repository.events(run.id(), new TenantId(1), 2, 0, 20_000);
                            return null;
                        });
        start.countDown();
        assertDoesNotThrow(
                () -> {
                    writer.get();
                    reader.get();
                });
        pool.shutdownNow();
    }

    @Test
    void coversGenerationLinkTerminalUpdateAndEventQueryBoundaries() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(3),
                                4,
                                null,
                                null,
                                "conversation",
                                null,
                                null,
                                null,
                                Map.of()),
                        AiRunSource.API,
                        "api",
                        "model",
                        "prompt");

        assertEquals(1, repository.linkGeneration(run.id(), new TenantId(3), 4, "generation-1"));
        assertEquals(0, repository.linkGeneration(run.id(), new TenantId(3), 4, "generation-2"));
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.linkGeneration(run.id(), new TenantId(99), 4, "generation-3"));
        assertTrue(repository.findByGeneration("generation-1", new TenantId(3), 4).isPresent());

        AiRun current = repository.find(run.id(), new TenantId(3), 4).orElseThrow();
        assertThrows(
                IllegalStateException.class,
                () ->
                        repository.updateTerminalAndAppend(
                                current.transition(AiRunStatus.COMPLETED),
                                current.version() - 1,
                                AiRunEventType.RUN_COMPLETED,
                                "done",
                                true));
        AiRun completed = current.transition(AiRunStatus.COMPLETED);
        AiRun result =
                repository.updateTerminalAndAppend(
                        completed, current.version(), AiRunEventType.RUN_COMPLETED, "done", true);
        assertEquals(2, result.lastEventSeq());
        assertTrue(
                repository.events(run.id(), new TenantId(3), 4, 0, 10).stream()
                        .anyMatch(event -> event.type() == AiRunEventType.RUN_COMPLETED));
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.events(run.id(), new TenantId(9), 4, 0, 10));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        repository.appendNextEvent(
                                "missing",
                                new TenantId(3),
                                4,
                                AiRunEventType.MODEL_DELTA,
                                "{}",
                                false,
                                Instant.now()));
    }

    @Test
    void ordersRunsAndLatestLookupsByIdWhenTimestampsTie() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        repository.insert(run("run-b", "generation", "execution"));
        repository.insert(run("run-a", "generation", "execution"));

        assertEquals("run-a", repository.list(new TenantId(1), 2, 1).getFirst().id());
        assertEquals(
                "run-a",
                repository.findByGeneration("generation", new TenantId(1), 2).orElseThrow().id());
        assertEquals(
                "run-a",
                repository.findByExecution("execution", new TenantId(1), 2).orElseThrow().id());
    }

    private static AiRun run(String id, String generationId, String executionId) {
        return new AiRun(
                id,
                new TenantId(1),
                new com.shiyu.ai.kernel.context.UserId(2),
                null,
                null,
                AiRunSource.API,
                "api-" + id,
                null,
                null,
                null,
                generationId,
                executionId,
                "model",
                "prompt",
                AiRunStatus.RUNNING,
                0,
                0,
                false,
                null,
                CREATED_AT,
                null,
                null,
                0);
    }
}

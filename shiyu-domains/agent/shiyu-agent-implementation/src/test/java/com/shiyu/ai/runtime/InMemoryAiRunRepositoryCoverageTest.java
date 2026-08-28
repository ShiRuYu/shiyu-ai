package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAiRunRepositoryCoverageTest {
    @Test
    void enforcesTenantScopeVersionAndTerminalEventIdempotency() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run = runtime.startRun(new AiRunContext(new TenantId(1), 2, null, null, "c", "g", null, null, Map.of()),
                AiRunSource.API, "api", "model", "prompt");

        assertTrue(repository.find(run.id(), new TenantId(1), 2).isPresent());
        assertTrue(repository.find(run.id(), new TenantId(9), 2).isEmpty());
        assertEquals(1, repository.list(new TenantId(1), 2, 0).size());
        assertEquals(0, repository.findByGeneration("g", new TenantId(9), 2).stream().count());
        assertEquals(1, repository.findByExecution(null, new TenantId(1), 2).stream().count());
        assertEquals(0, repository.update(run, -1));
        assertEquals(0, repository.update(run, run.version() - 1),
                "a stale CAS must fail even when the caller retries the same instance");
        assertThrows(IllegalStateException.class, () -> repository.insert(run));

        runtime.finish(run.id(), new TenantId(1), 2, AiRunStatus.COMPLETED, "done");
        long terminalSeq = runtime.events(run.id(), new TenantId(1), 2, 0, 10).getLast().seq();
        assertEquals(terminalSeq, repository.appendNextEvent(run.id(), new TenantId(1), 2,
                AiRunEventType.RUN_COMPLETED, "{\"errorCode\":\"done\"}", true, Instant.now()));
        assertThrows(IllegalStateException.class, () -> repository.appendNextEvent(run.id(), new TenantId(1), 2,
                AiRunEventType.RUN_FAILED, "{}", true, Instant.now()));
        assertThrows(IllegalStateException.class, () -> repository.appendNextEvent(run.id(), new TenantId(1), 2,
                AiRunEventType.RUN_COMPLETED, "different", true, Instant.now()));
    }

    @Test
    void rejectsNonContiguousExternalEventsAndAcceptsIdempotentReplay() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run = runtime.startRun(new AiRunContext(new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                AiRunSource.API, "api", "model", "prompt");
        AiRunEvent event = new AiRunEvent(run.id(), new TenantId(1), 3, AiRunEventType.MODEL_DELTA, "{}", true, Instant.now());
        assertThrows(IllegalStateException.class, () -> repository.appendEvent(event));
        AiRunEvent first = new AiRunEvent(run.id(), new TenantId(1), 2, AiRunEventType.MODEL_DELTA, "{}", true, Instant.now());
        assertEquals(2, repository.appendEvent(first));
        assertEquals(2, repository.appendEvent(first));
        assertThrows(IllegalStateException.class, () -> repository.appendEvent(
                new AiRunEvent(run.id(), new TenantId(1), 2, AiRunEventType.MODEL_DELTA, "other", true, Instant.now())));
    }

    @Test
    void coversGenerationLinkTerminalUpdateAndEventQueryBoundaries() {
        InMemoryAiRunRepository repository = new InMemoryAiRunRepository();
        AiRuntimeService runtime = new AiRuntimeService(repository, new InMemoryAiAppRepository());
        AiRun run = runtime.startRun(new AiRunContext(new TenantId(3), 4, null, null, "conversation", null, null, null, Map.of()),
                AiRunSource.API, "api", "model", "prompt");

        assertEquals(1, repository.linkGeneration(run.id(), new TenantId(3), 4, "generation-1"));
        assertEquals(0, repository.linkGeneration(run.id(), new TenantId(3), 4, "generation-2"));
        assertThrows(IllegalArgumentException.class,
                () -> repository.linkGeneration(run.id(), new TenantId(99), 4, "generation-3"));
        assertTrue(repository.findByGeneration("generation-1", new TenantId(3), 4).isPresent());

        AiRun current = repository.find(run.id(), new TenantId(3), 4).orElseThrow();
        assertThrows(IllegalStateException.class,
                () -> repository.updateTerminalAndAppend(current.transition(AiRunStatus.COMPLETED),
                        current.version() - 1, AiRunEventType.RUN_COMPLETED, "done", true));
        AiRun completed = current.transition(AiRunStatus.COMPLETED);
        AiRun result = repository.updateTerminalAndAppend(completed, current.version(),
                AiRunEventType.RUN_COMPLETED, "done", true);
        assertEquals(2, result.lastEventSeq());
        assertTrue(repository.events(run.id(), new TenantId(3), 4, 0, 10).stream()
                .anyMatch(event -> event.type() == AiRunEventType.RUN_COMPLETED));
        assertThrows(IllegalArgumentException.class, () -> repository.events(run.id(), new TenantId(9), 4, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> repository.appendNextEvent("missing", new TenantId(3), 4,
                AiRunEventType.MODEL_DELTA, "{}", false, Instant.now()));
    }
}

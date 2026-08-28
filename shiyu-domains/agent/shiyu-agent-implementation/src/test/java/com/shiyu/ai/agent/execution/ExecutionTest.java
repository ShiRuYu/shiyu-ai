package com.shiyu.ai.agent.execution;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionTest {

    @Test
    void restorePreservesPersistedExecutionState() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 5, 2, 30);
        LocalDateTime end = start.plusSeconds(2);
        Map<String, Object> input = Map.of("message", "hello");
        Map<String, Object> output = Map.of("answer", "world");

        Execution execution = Execution.restore(
                "execution-1", "agent-1", "v1", ExecutionStatus.COMPLETED,
                input, output, null, 42L, "session-1", start, end, 2_000L);

        assertEquals("execution-1", execution.getExecutionId());
        assertEquals("agent-1", execution.getAgentId());
        assertEquals("v1", execution.getVersion());
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertSame(input, execution.getInput());
        assertSame(output, execution.getOutput());
        assertEquals(42L, execution.getUserId());
        assertEquals("session-1", execution.getSessionId());
        assertEquals(start, execution.getStartTime());
        assertEquals(end, execution.getEndTime());
        assertEquals(2_000L, execution.getDurationMs());
    }

    @Test
    void awaitResumeOrCancellationUnblocksForResumeAndCancellation() throws Exception {
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.start();
        execution.pause();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> resumed = executor.submit(execution::awaitResumeOrCancellation);
            assertFalse(resumed.isDone());
            execution.resume();
            assertTrue(resumed.get(1, TimeUnit.SECONDS));

            execution.pause();
            Future<Boolean> cancelled = executor.submit(execution::awaitResumeOrCancellation);
            assertFalse(cancelled.isDone());
            execution.cancel();
            assertFalse(cancelled.get(1, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }
}

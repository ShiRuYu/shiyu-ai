package com.shiyu.ai.agent.implementation.runtime.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 验证取消标记仅影响被标记的工作线程，并在清除后失效。 */
class AgentCancellationLogContextTest {

    @Test
    void marksAndClearsOnlyTheCancelledWorkerThread() {
        Thread cancelled = new Thread();
        Thread unrelated = new Thread();

        assertFalse(AgentCancellationLogContext.isMarked(cancelled.threadId()));

        AgentCancellationLogContext.mark(cancelled);
        assertTrue(AgentCancellationLogContext.isMarked(cancelled.threadId()));
        assertFalse(AgentCancellationLogContext.isMarked(unrelated.threadId()));

        AgentCancellationLogContext.clear(cancelled);
        assertFalse(AgentCancellationLogContext.isMarked(cancelled.threadId()));
    }

    @Test
    void ignoresMissingWorkerWhenMarkingOrClearing() {
        Thread worker = Thread.currentThread();
        AgentCancellationLogContext.mark(worker);
        try {
            AgentCancellationLogContext.mark(null);
            AgentCancellationLogContext.clear(null);
            assertTrue(AgentCancellationLogContext.isMarked(worker.threadId()));
        } finally {
            AgentCancellationLogContext.clear(worker);
        }
    }
}

package com.shiyu.ai.agent.implementation.runtime.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks graph worker threads while they unwind an explicit stream cancellation. */
public final class AgentCancellationLogContext {

    private static final Set<Long> CANCELLED_THREAD_IDS = ConcurrentHashMap.newKeySet();

    private AgentCancellationLogContext() {}

    /** Mark a worker before interrupting it so the logger can recognize cancellation control flow. */
    public static void mark(Thread worker) {
        if (worker != null) CANCELLED_THREAD_IDS.add(worker.threadId());
    }

    /** Remove the cancellation marker when the worker has finished unwinding. */
    public static void clear(Thread worker) {
        if (worker != null) CANCELLED_THREAD_IDS.remove(worker.threadId());
    }

    /** Return whether the Log4j event's originating thread is unwinding a requested cancellation. */
    public static boolean isMarked(long threadId) {
        return CANCELLED_THREAD_IDS.contains(threadId);
    }
}

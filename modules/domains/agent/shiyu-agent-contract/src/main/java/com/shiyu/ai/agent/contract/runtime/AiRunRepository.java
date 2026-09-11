package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Persistence port for tenant-scoped agent runs and their append-only events. */
public interface AiRunRepository {
    /** Persists a newly created run. */
    void insert(AiRun run);

    /** Finds a run owned by the supplied tenant and user. */
    Optional<AiRun> find(String id, TenantId tenantId, long ownerUserId);

    /** Lists the owner's most recent runs up to the requested limit. */
    List<AiRun> list(TenantId tenantId, long ownerUserId, int limit);

    /** Finds the runtime run linked to a conversation generation. */
    Optional<AiRun> findByGeneration(String generationId, TenantId tenantId, long ownerUserId);

    /** Links a run to a generation using tenant and owner isolation. */
    int linkGeneration(String runId, TenantId tenantId, long ownerUserId, String generationId);

    /** Finds the runtime run linked to an agent execution. */
    Optional<AiRun> findByExecution(String executionId, TenantId tenantId, long ownerUserId);

    /** Updates a run only when its optimistic-lock version matches. */
    int update(AiRun run, long expectedVersion);

    /** Atomically persists a terminal state and its terminal event. */
    default AiRun updateTerminalAndAppend(
            AiRun run,
            long expectedVersion,
            AiRunEventType eventType,
            String payload,
            boolean redacted) {
        if (update(run, expectedVersion) != 1) throw new IllegalStateException("run was modified");
        long seq =
                appendNextEvent(
                        run.id(),
                        run.tenantId(),
                        run.ownerUserId().value(),
                        eventType,
                        payload == null ? "{}" : payload,
                        redacted,
                        Instant.now());
        return run.withLastEventSeq(seq);
    }

    /**
     * Allocates the next event sequence in the persistence layer. Implementations must make
     * allocation and insertion idempotent so callers do not need a JVM-wide lock.
     */
    default long appendNextEvent(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            AiRunEventType type,
            String payload,
            boolean redacted,
            Instant createdAt) {
        throw new UnsupportedOperationException(
                "database event sequence allocation is not configured");
    }

    default long appendNextEvent(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            AiRunEventType type,
            String payload,
            boolean redacted,
            Instant createdAt,
            String turnId,
            String stepId,
            String providerRequestId) {
        return appendNextEvent(runId, tenantId, ownerUserId, type, payload, redacted, createdAt);
    }

    /** Appends one immutable event and returns its sequence number. */
    long appendEvent(AiRunEvent event);

    /** Reads events after a sequence cursor for resumable runtime streams. */
    List<AiRunEvent> events(
            String runId, TenantId tenantId, long ownerUserId, long afterSeq, int limit);
}

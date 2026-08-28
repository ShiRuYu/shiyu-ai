package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface AiRunRepository {
    void insert(AiRun run);
    Optional<AiRun> find(String id, TenantId tenantId, long ownerUserId);
    List<AiRun> list(TenantId tenantId, long ownerUserId, int limit);
    Optional<AiRun> findByGeneration(String generationId, TenantId tenantId, long ownerUserId);
    int linkGeneration(String runId, TenantId tenantId, long ownerUserId, String generationId);
    Optional<AiRun> findByExecution(String executionId, TenantId tenantId, long ownerUserId);
    int update(AiRun run, long expectedVersion);
    /** Atomically persists a terminal state and its terminal event. */
    default AiRun updateTerminalAndAppend(AiRun run, long expectedVersion, AiRunEventType eventType,
                                          String payload, boolean redacted) {
        if (update(run, expectedVersion) != 1) throw new IllegalStateException("run was modified");
        long seq = appendNextEvent(run.id(), run.tenantId(), run.ownerUserId().value(), eventType,
                payload == null ? "{}" : payload, redacted, Instant.now());
        return run.withLastEventSeq(seq);
    }
    /**
     * Allocates the next event sequence in the persistence layer. Implementations must make
     * allocation and insertion idempotent so callers do not need a JVM-wide lock.
     */
    default long appendNextEvent(String runId, TenantId tenantId, long ownerUserId, AiRunEventType type,
                                  String payload, boolean redacted, Instant createdAt) {
        throw new UnsupportedOperationException("database event sequence allocation is not configured");
    }
    default long appendNextEvent(String runId, TenantId tenantId, long ownerUserId, AiRunEventType type,
                                 String payload, boolean redacted, Instant createdAt,
                                 String turnId, String stepId, String providerRequestId) {
        return appendNextEvent(runId, tenantId, ownerUserId, type, payload, redacted, createdAt);
    }
    long appendEvent(AiRunEvent event);
    List<AiRunEvent> events(String runId, TenantId tenantId, long ownerUserId, long afterSeq, int limit);
}

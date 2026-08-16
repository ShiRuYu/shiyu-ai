package com.shiyu.ai.runtime;

import java.util.List;
import java.util.Optional;

public interface AiRunRepository {
    void insert(AiRun run);
    Optional<AiRun> find(String id, long tenantId, long ownerUserId);
    default Optional<AiRun> findByGeneration(String generationId, long tenantId, long ownerUserId) { return Optional.empty(); }
    default Optional<AiRun> findByExecution(String executionId, long tenantId, long ownerUserId) { return Optional.empty(); }
    int update(AiRun run, long expectedVersion);
    long appendEvent(AiRunEvent event);
    List<AiRunEvent> events(String runId, long tenantId, long ownerUserId, long afterSeq, int limit);
}

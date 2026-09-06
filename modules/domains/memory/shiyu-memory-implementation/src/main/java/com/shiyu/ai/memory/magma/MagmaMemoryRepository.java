package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface MagmaMemoryRepository {
    void insertEvent(MemoryEvent event);
    Optional<MemoryEvent> findEvent(TenantId tenantId, String eventId);
    Optional<MemoryEvent> findLatestEvent(TenantId tenantId, String namespace, String subjectType, String subjectId);
    Optional<MemoryEvent> findPreviousEvent(TenantId tenantId, String namespace, String subjectType, String subjectId, Instant occurredAt);
    Optional<MemoryEvent> findNextEvent(TenantId tenantId, String namespace, String subjectType, String subjectId, Instant occurredAt);
    List<MemoryEvent> findCandidates(TenantId tenantId, String namespace, String subjectType, String subjectId, int limit);
    List<MemoryEvent> findByNamespace(TenantId tenantId, String namespace, int limit);
    void updateEventStatus(TenantId tenantId, String eventId, MemoryEventStatus status);
    void deactivateEdgesForNode(TenantId tenantId, String nodeId);
    void upsertEntity(MemoryEntity entity);
    void insertEdge(MemoryEdge edge);
    List<MemoryEdge> findEdges(TenantId tenantId, String nodeId, GraphType graphType, int limit);
    void enqueueConsolidation(TenantId tenantId, String eventId);
    void recordRetrievalTrace(MemoryRetrievalTrace trace);
    Optional<MemoryRetrievalTrace> findRetrievalTrace(TenantId tenantId, String traceId);
}

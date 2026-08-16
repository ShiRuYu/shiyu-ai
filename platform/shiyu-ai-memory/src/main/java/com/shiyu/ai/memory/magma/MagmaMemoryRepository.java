package com.shiyu.ai.memory.magma;

import java.util.List;
import java.util.Optional;

public interface MagmaMemoryRepository {
    void insertEvent(MemoryEvent event);
    Optional<MemoryEvent> findEvent(long tenantId, String eventId);
    Optional<MemoryEvent> findLatestEvent(long tenantId, String namespace, String subjectType, String subjectId);
    List<MemoryEvent> findCandidates(long tenantId, String namespace, String subjectType, String subjectId, int limit);
    default List<MemoryEvent> findByNamespace(long tenantId, String namespace, int limit) { return List.of(); }
    default List<MemoryEvent> findByNamespace(String namespace, int limit) { return List.of(); }
    void updateEventStatus(long tenantId, String eventId, MemoryEventStatus status);
    default void deactivateEdgesForNode(long tenantId, String nodeId) { }
    void upsertEntity(MemoryEntity entity);
    void insertEdge(MemoryEdge edge);
    List<MemoryEdge> findEdges(long tenantId, String nodeId, GraphType graphType, int limit);
    void enqueueConsolidation(long tenantId, String eventId);
    default void recordRetrievalTrace(MemoryRetrievalTrace trace) { }
    default Optional<MemoryRetrievalTrace> findRetrievalTrace(long tenantId, String traceId) { return Optional.empty(); }
}

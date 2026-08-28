package com.shiyu.ai.memory.magma;

import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class MagmaMemoryServiceTest {

    private static final TenantId TENANT = new TenantId(7L);
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-25T00:00:00Z");
    private final MagmaMemoryRepository repository = mock(MagmaMemoryRepository.class);
    private final MemorySemanticIndex semanticIndex = mock(MemorySemanticIndex.class);
    private final MagmaMemoryService service = new MagmaMemoryService(repository, semanticIndex);

    @Test
    void memoryCommandsCarryARequiredTenantId() {
        IngestMemoryCommand command = command(ConfirmationPolicy.REQUIRED);

        assertEquals(new TenantId(7L), command.tenantId());
        assertThrows(IllegalArgumentException.class, () -> new IngestMemoryCommand(null, "magma", "USER", "7", "STUDY",
                "content", OCCURRED_AT, "TEST", "source", Map.of(), 0.5, 0.5, ConfirmationPolicy.REQUIRED));
    }

    @Test
    void ingestsCandidateEventsWithDurablePersistenceAndBestEffortIndexes() {
        doThrow(new IllegalStateException("index unavailable")).when(semanticIndex).upsert(any());
        IngestMemoryCommand command = command(ConfirmationPolicy.REQUIRED);

        MemoryEvent event = service.ingest(command);

        assertEquals(MemoryEventStatus.CANDIDATE, event.status());
        assertEquals(TENANT, event.tenantId());
        verify(repository).insertEvent(event);
        verify(repository).enqueueConsolidation(TENANT, event.id());
    }

    @Test
    void confirmsRevokesAndChecksAccessBeforeMutatingEvents() {
        MemoryEvent event = event("event-1", MemoryEventStatus.CANDIDATE);
        when(repository.findEvent(TENANT, "event-1")).thenReturn(Optional.of(event));

        service.confirm(TENANT, "event-1");
        verify(repository).updateEventStatus(TENANT, "event-1", MemoryEventStatus.ACTIVE);

        service.revoke(TENANT, "event-1");
        verify(repository).updateEventStatus(TENANT, "event-1", MemoryEventStatus.REVOKED);
        verify(repository).deactivateEdgesForNode(TENANT, "event-1");
        verify(semanticIndex).delete("event-1");

        when(repository.findEvent(TENANT, "missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.confirm(TENANT, "missing"));
    }

    @Test
    void accessPolicyAndSubjectFiltersPreventCrossSubjectReads() {
        MemoryEvent event = event("event-1", MemoryEventStatus.ACTIVE);
        when(repository.findEvent(TENANT, "event-1")).thenReturn(Optional.of(event));
        MagmaMemoryService denied = new MagmaMemoryService(repository, semanticIndex,
                (tenant, namespace, subjectType, subjectId, sourceType, sourceId) -> false);
        assertThrows(IllegalArgumentException.class, () -> denied.requireAccessibleEvent(TENANT, "event-1"));

        MemoryQuery query = new MemoryQuery(new TenantId(7L), "magma", "USER", "8", "progress",
                Set.of(GraphType.SEMANTIC), null, null, 2, 10, 100);
        when(semanticIndex.search(query, 10)).thenReturn(List.of(new MemoryPath(event, 1.0, List.of())));
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(event));
        assertEquals(0, service.retrieve(query).size());
    }

    @Test
    void retrievesMatchingDurableAndSemanticAnchorsAndRecordsTrace() {
        MemoryEvent event = event("event-1", MemoryEventStatus.ACTIVE);
        MemoryQuery query = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                Set.of(GraphType.SEMANTIC), null, null, 2, 10, 100);
        when(semanticIndex.search(query, 10)).thenReturn(List.of(new MemoryPath(event, 1.0, List.of())));
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(event));

        var result = service.retrieveWithTrace(query);
        assertEquals(1, result.paths().size());
        assertEquals("event-1", result.paths().getFirst().event().id());
        verify(repository).recordRetrievalTrace(any(MemoryRetrievalTrace.class));
    }

    @Test
    void linksTemporalNeighborsAndTreatsIndexAndQueueAsBestEffort() {
        MemoryEvent previous = event("previous", MemoryEventStatus.ACTIVE);
        MemoryEvent next = event("next", MemoryEventStatus.ACTIVE);
        when(repository.findPreviousEvent(TENANT, "magma", "USER", "7", OCCURRED_AT)).thenReturn(Optional.of(previous));
        when(repository.findNextEvent(TENANT, "magma", "USER", "7", OCCURRED_AT)).thenReturn(Optional.of(next));
        when(repository.findEdges(eq(TENANT), anyString(), eq(GraphType.TEMPORAL), eq(200))).thenReturn(List.of());
        doThrow(new IllegalStateException("queue down")).when(repository).enqueueConsolidation(eq(TENANT), anyString());
        MemoryEvent created = service.ingest(command(ConfirmationPolicy.AUTO));
        assertEquals(MemoryEventStatus.ACTIVE, created.status());
        verify(repository, times(2)).insertEdge(any(MemoryEdge.class));
    }

    @Test
    void skipsExistingTemporalEdgesAndHandlesRevokeAndSupersedeIndexFailures() {
        MemoryEvent previous = event("previous", MemoryEventStatus.ACTIVE);
        when(repository.findPreviousEvent(any(TenantId.class), anyString(), anyString(), anyString(), any())).thenReturn(Optional.of(previous));
        when(repository.findEdges(eq(TENANT), anyString(), eq(GraphType.TEMPORAL), eq(200)))
                .thenReturn(List.of(edge("old-edge", "previous", "existing", "after")));
        doThrow(new IllegalStateException("delete unavailable")).when(semanticIndex).delete(anyString());
        when(repository.findEvent(TENANT, "event-1")).thenReturn(Optional.of(event("event-1", MemoryEventStatus.ACTIVE)));
        service.revoke(TENANT, "event-1");
        verify(repository).deactivateEdgesForNode(TENANT, "event-1");
        when(repository.findEvent(TENANT, "old")).thenReturn(Optional.of(event("old", MemoryEventStatus.ACTIVE)));
        service.supersede(TENANT, "old", command(ConfirmationPolicy.AUTO));
        verify(repository).updateEventStatus(TENANT, "old", MemoryEventStatus.SUPERSEDED);
    }

    @Test
    void validatesTraceOwnershipAndSubjectConstraints() {
        MemoryEvent event = event("event-1", MemoryEventStatus.ACTIVE);
        MemoryRetrievalTrace trace = new MemoryRetrievalTrace("trace-1", TENANT, "magma", "progress",
                List.of("event-1"), Map.of(), List.of(), List.of(), List.of("event-1"), Instant.now());
        when(repository.findRetrievalTrace(TENANT, "trace-1")).thenReturn(Optional.of(trace));
        when(repository.findEvent(TENANT, "event-1")).thenReturn(Optional.of(event));
        assertEquals(trace, service.trace(TENANT, "trace-1", "USER", "7"));
        assertEquals(trace, service.trace(TENANT, "trace-1"));
        assertThrows(IllegalArgumentException.class, () -> service.trace(TENANT, "trace-1", "GROUP", "9"));
        when(repository.findRetrievalTrace(TENANT, "missing")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.trace(TENANT, "missing"));
    }

    @Test
    void handlesSemanticFailureFilteringDatesAndGraphExpansion() {
        MemoryEvent anchor = event("anchor", MemoryEventStatus.ACTIVE);
        MemoryEvent related = event("related", MemoryEventStatus.ACTIVE);
        MemoryEvent revoked = event("revoked", MemoryEventStatus.REVOKED);
        MemoryEvent noise = new MemoryEvent("noise", TENANT, "magma", "USER", "7", "STUDY", "unrelated",
                OCCURRED_AT, "conversation", "source-1", Map.of(), 1, 1, MemoryEventStatus.ACTIVE,
                ConfirmationPolicy.AUTO, OCCURRED_AT, OCCURRED_AT);
        MemoryQuery query = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                Set.of(GraphType.SEMANTIC, GraphType.TEMPORAL), OCCURRED_AT.minusSeconds(1), OCCURRED_AT.plusSeconds(1), 2, 5, 100,
                MemoryQueryIntent.HYBRID);
        doThrow(new IllegalStateException("semantic down")).when(semanticIndex).search(query, 5);
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(anchor, revoked, noise));
        when(repository.findEdges(TENANT, "anchor", GraphType.SEMANTIC, 20)).thenReturn(List.of(edge("e1", "anchor", "related", "links")));
        when(repository.findEdges(TENANT, "anchor", GraphType.TEMPORAL, 20)).thenReturn(List.of());
        when(repository.findEvent(TENANT, "related")).thenReturn(Optional.of(related));
        var result = service.retrieveWithTrace(query);
        assertFalse(result.paths().isEmpty());
        verify(repository).recordRetrievalTrace(any(MemoryRetrievalTrace.class));

        MemoryQuery capped = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                Set.of(GraphType.SEMANTIC), null, null, 0, 1, 100);
        when(semanticIndex.search(capped, 1)).thenReturn(List.of(new MemoryPath(anchor, 1, List.of())));
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(anchor));
        assertEquals(1, service.retrieveWithTrace(capped).paths().size());
    }

    @Test
    void exercisesGraphIntentWeightsRelationsAndQueryFilters() {
        MemoryEvent event = event("event-filter", MemoryEventStatus.ACTIVE);
        when(repository.findEvent(TENANT, "event-filter")).thenReturn(Optional.of(event));
        when(repository.findEdges(eq(TENANT), eq("event-filter"), any(GraphType.class), anyInt())).thenReturn(List.of());
        assertTrue(service.relations(TENANT, "event-filter", GraphType.ENTITY, 0).isEmpty());
        assertTrue(service.relations(TENANT, "event-filter", GraphType.ENTITY, 500).isEmpty());

        for (MemoryQueryIntent intent : List.of(MemoryQueryIntent.SEMANTIC, MemoryQueryIntent.TEMPORAL,
                MemoryQueryIntent.CAUSAL, MemoryQueryIntent.ENTITY, MemoryQueryIntent.HYBRID)) {
            MemoryQuery query = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                    Set.of(GraphType.SEMANTIC, GraphType.TEMPORAL, GraphType.CAUSAL, GraphType.ENTITY),
                    null, null, 4, 20, 100, intent);
            when(semanticIndex.search(query, 4)).thenReturn(List.of());
            when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(event));
            service.retrieveWithTrace(query);
        }

        MemoryQuery wrongSubject = new MemoryQuery(new TenantId(7L), "magma", "OTHER", "9", "progress",
                Set.of(), null, null, 1, 1, 10);
        when(semanticIndex.search(wrongSubject, 1)).thenReturn(List.of(new MemoryPath(event, 1, List.of())));
        assertTrue(service.retrieve(wrongSubject).isEmpty());
    }

    @Test
    void enforcesTracePolicyAndHandlesTokenBudgetDuringExpansion() {
        MemoryEvent anchor = event("anchor-budget", MemoryEventStatus.ACTIVE);
        MemoryEvent related = new MemoryEvent("related-budget", TENANT, "magma", "USER", "7", "STUDY",
                "a very long related memory", OCCURRED_AT, "conversation", "source-2", Map.of(), 1, 1,
                MemoryEventStatus.ACTIVE, ConfirmationPolicy.AUTO, OCCURRED_AT, OCCURRED_AT);
        MemoryEdge reverse = edge("reverse", "related-budget", "anchor-budget", "links");
        MemoryQuery query = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                Set.of(GraphType.TEMPORAL), null, null, 2, 10, 1, MemoryQueryIntent.TEMPORAL);
        when(semanticIndex.search(query, 2)).thenReturn(List.of(new MemoryPath(anchor, 1, List.of())));
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(anchor));
        when(repository.findEdges(TENANT, "anchor-budget", GraphType.TEMPORAL, 20)).thenReturn(List.of(reverse));
        when(repository.findEvent(TENANT, "related-budget")).thenReturn(Optional.of(related));
        assertEquals(1, service.retrieveWithTrace(query).paths().size(), "budget must reject the oversized neighbor");

        MemoryRetrievalTrace trace = new MemoryRetrievalTrace("denied-trace", TENANT, "magma", "progress",
                List.of("anchor-budget"), Map.of(), List.of(), List.of(), List.of("anchor-budget"), Instant.now());
        when(repository.findRetrievalTrace(TENANT, "denied-trace")).thenReturn(Optional.of(trace));
        when(repository.findEvent(TENANT, "anchor-budget")).thenReturn(Optional.of(anchor));
        MagmaMemoryService denied = new MagmaMemoryService(repository, semanticIndex,
                (tenant, namespace, subjectType, subjectId, sourceType, sourceId) -> false);
        assertThrows(IllegalArgumentException.class, () -> denied.trace(TENANT, "denied-trace"));
    }

    @Test
    void filtersSemanticAndDurableCandidatesByNamespaceDatesAndPolicy() {
        MemoryEvent matching = event("matching", MemoryEventStatus.ACTIVE);
        MemoryEvent wrongNamespace = new MemoryEvent("wrong-ns", TENANT, "other", "USER", "7", "STUDY", "progress",
                OCCURRED_AT, "conversation", "source-1", Map.of(), 1, 1, MemoryEventStatus.ACTIVE,
                ConfirmationPolicy.AUTO, OCCURRED_AT, OCCURRED_AT);
        MemoryEvent future = new MemoryEvent("future", TENANT, "magma", "USER", "7", "STUDY", "progress",
                OCCURRED_AT.plusSeconds(100), "conversation", "source-1", Map.of(), 1, 1, MemoryEventStatus.ACTIVE,
                ConfirmationPolicy.AUTO, OCCURRED_AT, OCCURRED_AT);
        MemoryQuery query = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                Set.of(GraphType.SEMANTIC), OCCURRED_AT.minusSeconds(1), OCCURRED_AT.plusSeconds(1), 2, 10, 100);
        when(semanticIndex.search(query, 10)).thenReturn(List.of(new MemoryPath(matching, 1, List.of()),
                new MemoryPath(wrongNamespace, 1, List.of()), new MemoryPath(future, 1, List.of())));
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(matching, wrongNamespace, future));
        assertEquals(1, service.retrieveWithTrace(query).paths().size());

        MemoryQuery unrestricted = new MemoryQuery(new TenantId(7L), "magma", "", "", "progress",
                Set.of(GraphType.SEMANTIC), null, null, 2, 10, 100);
        when(semanticIndex.search(unrestricted, 10)).thenReturn(List.of(new MemoryPath(matching, 1, List.of())));
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(matching));
        MagmaMemoryService denied = new MagmaMemoryService(repository, semanticIndex,
                (tenant, namespace, subjectType, subjectId, sourceType, sourceId) -> false);
        assertTrue(denied.retrieve(unrestricted).isEmpty());
    }

    @Test
    void treatsMatchingTemporalEdgesAsIdempotentAndDefaultsNullPolicyToAllow() throws Exception {
        MagmaMemoryService permissive = new MagmaMemoryService(repository, semanticIndex, null);
        MemoryEvent event = event("event-policy", MemoryEventStatus.ACTIVE);
        when(repository.findEvent(TENANT, "event-policy")).thenReturn(Optional.of(event));
        assertEquals(event, permissive.requireAccessibleEvent(TENANT, "event-policy"));

        when(repository.findEdges(TENANT, "source", GraphType.TEMPORAL, 200))
                .thenReturn(List.of(edge("edge", "source", "target", "after")));
        Method insert = MagmaMemoryService.class.getDeclaredMethod("insertTemporalEdgeIfMissing", TenantId.class,
                String.class, String.class, String.class, String.class, Instant.class);
        insert.setAccessible(true);
        insert.invoke(service, TENANT, "source", "target", "after", "evidence", OCCURRED_AT);
        verify(repository, never()).insertEdge(any(MemoryEdge.class));
        insert.invoke(service, TENANT, "source", "target", "before", "evidence", OCCURRED_AT);
        verify(repository).insertEdge(any(MemoryEdge.class));
    }

    @Test
    void handlesPartialTraceSubjectAndSelfTemporalNeighbors() {
        MemoryEvent event = event("partial-trace", MemoryEventStatus.ACTIVE);
        MemoryRetrievalTrace trace = new MemoryRetrievalTrace("partial", TENANT, "magma", "progress",
                List.of("partial-trace"), Map.of(), List.of(), List.of(), List.of(), OCCURRED_AT);
        when(repository.findRetrievalTrace(TENANT, "partial")).thenReturn(Optional.of(trace));
        when(repository.findEvent(TENANT, "partial-trace")).thenReturn(Optional.of(event));

        assertEquals(trace, service.trace(TENANT, "partial", "USER", null));

    }

    @Test
    void rejectsEachMismatchedSubjectComponentIndependently() {
        MemoryEvent event = event("subject-filter", MemoryEventStatus.ACTIVE);
        when(semanticIndex.search(any(MemoryQuery.class), anyInt())).thenReturn(List.of(new MemoryPath(event, 1, List.of())));
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(event));

        MemoryQuery wrongType = new MemoryQuery(new TenantId(7L), "magma", "GROUP", "7", "progress",
                Set.of(GraphType.SEMANTIC), null, null, 1, 10, 100);
        MemoryQuery wrongId = new MemoryQuery(new TenantId(7L), "magma", "USER", "8", "progress",
                Set.of(GraphType.SEMANTIC), null, null, 1, 10, 100);
        assertTrue(service.retrieve(wrongType).isEmpty());
        assertTrue(service.retrieve(wrongId).isEmpty());

        MemoryQuery noSubjectFilter = new MemoryQuery(new TenantId(7L), "magma", null, null, "progress",
                Set.of(GraphType.SEMANTIC), null, null, 1, 10, 100);
        assertEquals(1, service.retrieve(noSubjectFilter).size());
    }

    @Test
    void appliesEachOptionalDateBoundIndependently() {
        MemoryEvent event = event("date-filter", MemoryEventStatus.ACTIVE);
        when(semanticIndex.search(any(MemoryQuery.class), anyInt())).thenReturn(List.of());
        when(repository.findByNamespace(TENANT, "magma", 1000)).thenReturn(List.of(event));

        MemoryQuery fromOnly = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                Set.of(), OCCURRED_AT.minusSeconds(1), null, 1, 10, 100);
        MemoryQuery toOnly = new MemoryQuery(new TenantId(7L), "magma", "USER", "7", "progress",
                Set.of(), null, OCCURRED_AT.plusSeconds(1), 1, 10, 100);
        assertEquals(1, service.retrieve(fromOnly).size());
        assertEquals(1, service.retrieve(toOnly).size());
    }

    @Test
    void tokenCountHandlesNullAndNonNullContent() throws Exception {
        Method tokenCount = MagmaMemoryService.class.getDeclaredMethod("tokenCount", String.class);
        tokenCount.setAccessible(true);
        assertEquals(1, tokenCount.invoke(service, new Object[]{null}));
        assertEquals(1, tokenCount.invoke(service, "abcd"));
    }

    private static MemoryEdge edge(String id, String source, String target, String relation) {
        return new MemoryEdge(id, TENANT, source, target, GraphType.TEMPORAL, relation, true,
                1.0, 1.0, EdgeOrigin.RULE, "test", true, OCCURRED_AT);
    }

    private static IngestMemoryCommand command(ConfirmationPolicy policy) {
        return new IngestMemoryCommand(new TenantId(7L), "magma", "USER", "7", "STUDY",
                "progress", OCCURRED_AT, "conversation", "source-1", Map.of(), 1.2, -0.1, policy);
    }

    private static MemoryEvent event(String id, MemoryEventStatus status) {
        return new MemoryEvent(id, TENANT, "magma", "USER", "7", "STUDY", "progress",
                OCCURRED_AT, "conversation", "source-1", Map.of(), 1.0, 1.0,
                status, ConfirmationPolicy.AUTO, OCCURRED_AT, OCCURRED_AT);
    }
}

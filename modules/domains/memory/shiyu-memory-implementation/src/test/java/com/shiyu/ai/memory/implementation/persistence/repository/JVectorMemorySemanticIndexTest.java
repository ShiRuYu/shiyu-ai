package com.shiyu.ai.memory.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.vector.api.VectorStore;
import com.shiyu.ai.common.vector.config.VectorStoreProperties;
import com.shiyu.ai.common.vector.model.VectorRecord;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;
import com.shiyu.ai.model.contract.api.EmbeddingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * 验证 J 向量 记忆 Semantic 索引 相关功能、边界条件、异常路径和协作行为。
 */
class JVectorMemorySemanticIndexTest {
    private static final TenantId TENANT = new TenantId(7L);

    @Test
    void upsertSearchDeleteAndNamespaceRebuildApplyTenantFilters() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(8);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        EmbeddingService embeddings = embeddings(8);
        JVectorMemorySemanticIndex index =
                new JVectorMemorySemanticIndex(
                        store, properties, repository, provider(embeddings), "test-profile");
        MemoryEvent event = event("e1", "hello world", MemoryEventStatus.ACTIVE);
        when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                .thenReturn(List.of(vectorRecord("e1", 8, "test-profile", 0.8d)));
        when(repository.findEventsByIds(TENANT, List.of("e1"))).thenReturn(List.of(event));
        when(repository.findEventsForSemanticIndexRebuild(TENANT, "notes", "", 1000))
                .thenReturn(List.of(event));

        index.upsert(event);
        List<MemoryPath> found =
                index.search(
                        new MemoryQuery(
                                new TenantId(7L),
                                "notes",
                                "PROFILE",
                                "u1",
                                "hello",
                                Set.of(),
                                null,
                                null,
                                2,
                                20,
                                100),
                        5);
        index.delete("e1");
        index.rebuild(TENANT, "notes");

        assertEquals(1, found.size());
        assertEquals("e1", found.get(0).event().id());
        verify(store).upsert(any(VectorRecord.class));
        verify(store).upsertBatch(anyList());
        verify(store, times(3)).flush();
        verify(store).delete("e1");
    }

    @Test
    void rebuildReembedsCandidateEventsForTheCurrentProfile() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index =
                new JVectorMemorySemanticIndex(
                        store, properties, repository, provider(embeddings(4)), "current-profile");
        MemoryEvent candidate = event("candidate-1", "pending memory", MemoryEventStatus.CANDIDATE);
        when(repository.findEventsForSemanticIndexRebuild(TENANT, "notes", "", 1000))
                .thenReturn(List.of(candidate));
        when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                .thenAnswer(invocation -> {
                    if (store.size() == 0) return List.of();
                    return List.of(
                            vectorRecord("candidate-1", 4, "current-profile", 0.0d));
                });

        index.rebuild(TENANT, "notes");

        verify(store).upsertBatch(
                org.mockito.ArgumentMatchers.argThat(
                        records ->
                                records.size() == 1
                                        && records.getFirst().id().equals("candidate-1")
                                        && records.getFirst()
                                                .metadata()
                                                .get("embeddingProfile")
                                                .equals(
                                                        "memory-embedding-service-v1:current-profile:4")));
    }

    @Test
    void rebuildCleanupIncludesNegativeSimilarityVectors() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index =
                new JVectorMemorySemanticIndex(
                        store, properties, repository, provider(embeddings(4)), "test-profile");
        VectorRecord stale = vectorRecord("stale-negative", 4, "test-profile", -0.25d);
        when(store.size()).thenReturn(1);
        when(repository.findEventsForSemanticIndexRebuild(TENANT, "notes", "", 1000))
                .thenReturn(List.of());
        when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                .thenAnswer(invocation -> {
                    var request = invocation.getArgument(
                            0, com.shiyu.ai.common.vector.model.VectorSearchRequest.class);
                    return request.getMinScore() <= -0.25d ? List.of(stale) : List.of();
                });

        index.rebuild(TENANT, "notes");

        verify(store).search(org.mockito.ArgumentMatchers.argThat(
                request -> request.getMinScore() <= -1.0d
                        && request.getSearchType()
                                == com.shiyu.ai.common.vector.model.VectorSearchType.EXACT));
        verify(store).deleteBatch(List.of("stale-negative"));
    }

    @Test
    void rebuildCleanupRetainsAnActiveEventAddedAfterThePagedScan() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index =
                new JVectorMemorySemanticIndex(
                        store, properties, repository, provider(embeddings(4)), "test-profile");
        MemoryEvent active = event("concurrent-active", "new memory", MemoryEventStatus.ACTIVE);
        when(store.size()).thenReturn(1);
        when(repository.findEventsForSemanticIndexRebuild(TENANT, "notes", "", 1000))
                .thenReturn(List.of());
        when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                .thenReturn(List.of(vectorRecord("concurrent-active", 4, "test-profile", 0.1d)));
        when(repository.findEventsByIds(TENANT, List.of("concurrent-active")))
                .thenReturn(List.of(active));

        index.rebuild(TENANT, "notes");

        verify(repository).findEventsByIds(TENANT, List.of("concurrent-active"));
        verify(store, never()).deleteBatch(anyList());
    }

    @Test
    void searchUsesOneVectorScanAndLoadsCandidateEventsInBatches() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index =
                new JVectorMemorySemanticIndex(
                        store, properties, repository, provider(embeddings(4)), "test-profile");
        MemoryEvent first = event("e1", "first", MemoryEventStatus.ACTIVE);
        MemoryEvent second = event("e2", "second", MemoryEventStatus.ACTIVE);
        when(store.size()).thenReturn(2);
        when(store.count(anyMap())).thenReturn(2);
        when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                .thenReturn(List.of(
                        vectorRecord("e1", 4, "test-profile", 0.9d),
                        vectorRecord("e2", 4, "test-profile", 0.8d)));
        when(store.count(anyMap())).thenReturn(2);
        when(repository.findEventsByIds(TENANT, List.of("e1", "e2")))
                .thenReturn(List.of(first, second));

        List<MemoryPath> found = index.search(query(), 1);

        assertEquals(1, found.size());
        assertEquals("e1", found.getFirst().event().id());
        verify(store, times(1))
                .search(
                        org.mockito.ArgumentMatchers.argThat(
                                request ->
                                        request.getTopK() == 4
                                                && request.getSearchType()
                                                        == com.shiyu.ai.common.vector.model.VectorSearchType.ANN
                                                && MemoryEventStatus.ACTIVE.name()
                                                        .equals(request.getFilter().get("status"))));
        verify(store, times(2)).count(anyMap());
        verify(repository).findEventsByIds(TENANT, List.of("e1", "e2"));
        verify(repository, never()).findEvent(any(TenantId.class), anyString());
    }

    @Test
    void searchRejectsLegacyProfileVectorsForTheTenantEvenAfterFillingTheResultLimit() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index =
                new JVectorMemorySemanticIndex(
                        store, properties, repository, provider(embeddings(4)), "test-profile");
        MemoryEvent active = event("e1", "current", MemoryEventStatus.ACTIVE);
        MemoryEvent legacy = event("e2", "legacy", MemoryEventStatus.ACTIVE);
        when(store.size()).thenReturn(2);
        when(store.count(anyMap())).thenReturn(2, 1);

        IllegalStateException failure = assertThrows(
                IllegalStateException.class, () -> index.search(query(), 1));

        assertTrue(failure.getMessage().contains("Incompatible semantic vector profile"));
        verify(store, never()).search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class));
        verify(store, times(2)).count(anyMap());
        verify(repository, never()).findEventsByIds(any(TenantId.class), anyList());
    }

    @Test
    void searchFiltersInactiveEventsAndHandlesOptionalSubjectFilters() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index =
                new JVectorMemorySemanticIndex(
                        store, properties, repository, provider(embeddings(4)), "test-profile");
        MemoryEvent active = event("e1", "active", MemoryEventStatus.ACTIVE);
        MemoryEvent inactive = event("e2", "inactive", MemoryEventStatus.SUPERSEDED);
        when(store.count(anyMap())).thenReturn(2);
        when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                .thenReturn(
                        List.of(
                                vectorRecord("e1", 4, "test-profile", 0.8d),
                                vectorRecord("e2", 4, "test-profile", 0.7d)));
        when(repository.findEventsByIds(TENANT, List.of("e1", "e2")))
                .thenReturn(List.of(active, inactive));

        List<MemoryPath> found =
                index.search(
                        new MemoryQuery(
                                new TenantId(7L),
                                "notes",
                                "",
                                "",
                                "query",
                                Set.of(),
                                null,
                                null,
                                1,
                                5,
                                20),
                        5);

        assertEquals(1, found.size());
        assertEquals("e1", found.getFirst().event().id());
        verify(store)
                .search(
                        org.mockito.ArgumentMatchers.argThat(
                                request ->
                                        request.getFilter()
                                                        .get("status")
                                                        .equals(MemoryEventStatus.ACTIVE.name())
                                                && !request.getFilter().containsKey("subjectType")
                                                && !request.getFilter().containsKey("subjectId")));
    }

    private static MemoryQuery query() {
        return new MemoryQuery(
                TENANT, "notes", "PROFILE", "u1", "query", Set.of(),
                null, null, 1, 5, 20);
    }

    @Test
    void missingOrIncompatibleEmbeddingCannotWriteAnIndex() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        MemoryEvent event = event("unavailable", "memory", MemoryEventStatus.ACTIVE);
        JVectorMemorySemanticIndex unconfigured = new JVectorMemorySemanticIndex(store, properties, repository);
        assertTrue(assertThrows(IllegalStateException.class, () -> unconfigured.upsert(event))
                .getMessage().contains("EmbeddingService is not configured"));
        for (int dimension : new int[] {0, 5}) {
            EmbeddingService embedding = embeddings(dimension);
            var index = new JVectorMemorySemanticIndex(store, properties, repository, provider(embedding), null);
            assertThrows(IllegalStateException.class, () -> index.upsert(event));
        }
        EmbeddingService embedding = embeddings(4);
        when(embedding.embed(any(TenantId.class), anyString())).thenReturn((float[]) null);
        var index = new JVectorMemorySemanticIndex(store, properties, repository, provider(embedding), " ");
        assertTrue(assertThrows(IllegalStateException.class, () -> index.upsert(event))
                .getMessage().contains("returned dimension 0"));
        when(embedding.embed(any(TenantId.class), anyString())).thenReturn(new float[3]);
        assertTrue(assertThrows(IllegalStateException.class, () -> index.upsert(event))
                .getMessage().contains("returned dimension 3"));
        properties.setDimension(0);
        var noDimension = new JVectorMemorySemanticIndex(store, properties, repository, provider(embedding), "test");
        assertThrows(IllegalStateException.class, () -> noDimension.upsert(event));
        verifyNoInteractions(store);
    }

    @Test
    void incompatiblePersistedVectorsFailClosedWithRebuildGuidance() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        var index = new JVectorMemorySemanticIndex(store, properties, repository, provider(embeddings(4)), "test");
        when(repository.findEventsByIds(TENANT, List.of("stale")))
                .thenReturn(List.of(event("stale", "content", MemoryEventStatus.ACTIVE)));
        VectorRecord valid = vectorRecord("stale", 4, "test", 0.8);
        for (String key : List.of("embeddingProfile", "embeddingDimension")) {
            Map<String, Object> metadata = new HashMap<>(valid.metadata());
            metadata.put(key, "old");
            when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                    .thenReturn(List.of(new VectorRecord("stale", valid.vector(), metadata)));
            assertTrue(assertThrows(IllegalStateException.class, () -> index.search(query(), 2))
                    .getMessage().contains("Rebuild"));
        }
        for (float[] vector : new float[][] {null, new float[3]}) {
            when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                    .thenReturn(List.of(new VectorRecord("stale", vector, valid.metadata())));
            assertTrue(assertThrows(IllegalStateException.class, () -> index.search(query(), 2))
                    .getMessage().contains("Stored memory vector dimension mismatch"));
        }
        assertTrue(index.search(query(), 0).isEmpty());
        verify(store, never()).upsert(any(VectorRecord.class));
    }

    @Test
    void staleMetadataCannotExposeAnEventFromADifferentSubject() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        var index = new JVectorMemorySemanticIndex(store, properties, repository, provider(embeddings(4)), "test");
        when(store.search(any(com.shiyu.ai.common.vector.model.VectorSearchRequest.class)))
                .thenReturn(List.of(vectorRecord("stale", 4, "test", 0.8)));
        MemoryEvent source = event("stale", "content", MemoryEventStatus.ACTIVE);
        for (List<String> scope : List.of(List.of("other", "PROFILE", "u1"),
                List.of("notes", "OTHER", "u1"), List.of("notes", "PROFILE", "u2"))) {
            MemoryEvent moved = new MemoryEvent(source.id(), TENANT, scope.get(0), scope.get(1), scope.get(2),
                    source.eventType(), source.content(), source.occurredAt(), source.sourceType(), source.sourceId(),
                    source.attributes(), source.confidence(), source.importance(), source.status(), source.confirmationPolicy(),
                    source.createdAt(), source.updatedAt());
            when(repository.findEventsByIds(TENANT, List.of("stale"))).thenReturn(List.of(moved));
            assertTrue(index.search(query(), 2).isEmpty());
        }
    }

    private static ObjectProvider<EmbeddingService> provider(EmbeddingService embeddings) {
        return new org.springframework.beans.factory.support.StaticListableBeanFactory(
                        Map.of("embeddingService", embeddings))
                .getBeanProvider(EmbeddingService.class);
    }

    private static EmbeddingService embeddings(int dimension) {
        EmbeddingService embeddings = mock(EmbeddingService.class);
        when(embeddings.dimension()).thenReturn(dimension);
        when(embeddings.embed(any(TenantId.class), anyString())).thenAnswer(invocation -> {
            float[] vector = new float[dimension];
            vector[0] = 1;
            return vector;
        });
        return embeddings;
    }

    private static VectorRecord vectorRecord(
            String id, int dimension, String profile, double score) {
        Map<String, Object> metadata =
                Map.of(
                        "_score", score,
                        "embeddingProfile", "memory-embedding-service-v1:" + profile + ":" + dimension,
                        "embeddingDimension", dimension,
                        "tenantId", TENANT.value(),
                        "namespace", "notes",
                        "subjectType", "PROFILE",
                        "subjectId", "u1",
                        "status", MemoryEventStatus.ACTIVE.name());
        float[] vector = new float[dimension];
        vector[0] = 1;
        return new VectorRecord(id, vector, metadata);
    }

    private static MemoryEvent event(String id, String content, MemoryEventStatus status) {
        Instant now = Instant.now();
        return new MemoryEvent(
                id,
                TENANT,
                "notes",
                "PROFILE",
                "u1",
                "OBSERVED",
                content,
                now,
                "TEST",
                id,
                Map.of(),
                0.8,
                0.5,
                status,
                ConfirmationPolicy.AUTO,
                now,
                now);
    }
}

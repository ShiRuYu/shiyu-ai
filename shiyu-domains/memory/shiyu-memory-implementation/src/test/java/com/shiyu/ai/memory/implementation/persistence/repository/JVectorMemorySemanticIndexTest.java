package com.shiyu.ai.memory.implementation.persistence.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.magma.*;
import com.shiyu.ai.vector.VectorRecord;
import com.shiyu.ai.vector.VectorStore;
import com.shiyu.ai.vector.config.VectorStoreProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JVectorMemorySemanticIndexTest {
    private static final TenantId TENANT = new TenantId(7L);
    @Test
    void upsertSearchDeleteAndNamespaceRebuildApplyTenantFilters() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(8);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index = new JVectorMemorySemanticIndex(store, properties, repository);
        MemoryEvent event = event("e1", "hello world", MemoryEventStatus.ACTIVE);
        when(store.search(any(com.shiyu.ai.vector.VectorSearchRequest.class))).thenReturn(
                List.of(new VectorRecord("e1", new float[]{1}, Map.of("_score", 0.8d))));
        when(repository.findEvent(TENANT, "e1")).thenReturn(Optional.of(event));
        when(repository.findByNamespace(TENANT, "notes", 100000)).thenReturn(List.of(event));

        index.upsert(event);
        List<MemoryPath> found = index.search(new MemoryQuery(new TenantId(7L), "notes", "PROFILE", "u1", "hello", Set.of(), null, null, 2, 20, 100), 5);
        index.delete("e1");
        index.rebuild(TENANT, "notes");

        assertEquals(1, found.size());
        assertEquals("e1", found.get(0).event().id());
        verify(store, times(2)).upsert(any(VectorRecord.class));
        verify(store, times(3)).flush();
        verify(store).delete("e1");
    }

    @Test
    void searchFiltersInactiveEventsAndHandlesOptionalSubjectFilters() {
        VectorStore store = mock(VectorStore.class);
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimension(4);
        JdbcMagmaMemoryRepository repository = mock(JdbcMagmaMemoryRepository.class);
        JVectorMemorySemanticIndex index = new JVectorMemorySemanticIndex(store, properties, repository);
        MemoryEvent inactive = event("e2", "old", MemoryEventStatus.REVOKED);
        when(store.search(any(com.shiyu.ai.vector.VectorSearchRequest.class))).thenReturn(
                List.of(new VectorRecord("e2", new float[]{1}, Map.of())));
        when(repository.findEvent(TENANT, "e2")).thenReturn(Optional.of(inactive));

        assertTrue(index.search(new MemoryQuery(new TenantId(7L), "notes", "", "", "query", Set.of(), null, null, 1, 5, 20), 5).isEmpty());
    }

    private static MemoryEvent event(String id, String content, MemoryEventStatus status) {
        Instant now = Instant.now();
        return new MemoryEvent(id, TENANT, "notes", "PROFILE", "u1", "OBSERVED", content,
                now, "TEST", id, Map.of(), 0.8, 0.5, status, ConfirmationPolicy.AUTO, now, now);
    }
}

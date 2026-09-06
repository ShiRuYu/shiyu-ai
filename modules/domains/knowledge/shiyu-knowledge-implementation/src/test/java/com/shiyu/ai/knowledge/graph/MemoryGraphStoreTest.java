package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeRelationBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MemoryGraphStoreTest {
    private static final TenantId TENANT = new TenantId(7L);

    @Test
    void buildsTenantSpaceGraphAndSupportsTraversalAndInvalidation() {
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeRelationRepository relations = mock(KnowledgeRelationRepository.class);
        when(knowledge.findById(eq(TENANT), anyLong())).thenAnswer(invocation -> {
            long id = invocation.getArgument(1);
            if (id < 1 || id > 3) return null;
            KnowledgeBO value = new KnowledgeBO(); value.setId(id); value.setSpaceId(10L); value.setName("N" + id); value.setCode("n" + id); return value;
        });
        when(knowledge.findBySpace(TENANT, 10L)).thenReturn(List.of(node(1L), node(2L), node(3L)));
        when(relations.findBySpace(TENANT, 10L)).thenReturn(List.of(
                relation(1L, 2L, "PRE"), relation(2L, 3L, "NEXT"),
                relation(1L, 3L, "RELATED"), relation(3L, 1L, "INCLUDE"), relation(3L, 2L, "UNKNOWN")));
        MemoryGraphStore store = new MemoryGraphStore(knowledge, relations);

        GraphNode first = store.getNode(TENANT, 1L);
        assertNotNull(first);
        assertEquals(List.of(2L), store.parents(TENANT, 1L));
        assertEquals(List.of(1L, 3L), store.children(TENANT, 2L));
        assertEquals(List.of(3L), store.related(TENANT, 1L));
        assertEquals(2, store.edges(TENANT, 1L).size());
        assertTrue(store.getParentNodes(TENANT, 1L).stream().allMatch(node -> node.getId() == 2L));
        assertEquals(2, store.getChildNodes(TENANT, 2L).size());
        assertEquals(1, store.getRelatedNodes(TENANT, 1L).size());
        assertTrue(store.edges(TENANT, 99L).isEmpty());

        assertEquals(List.of(2L, 1L), store.dfs(TENANT, 1L));
        assertEquals(List.of(1L, 2L), store.bfs(TENANT, 1L));
        assertEquals(List.of(1L, 2L), store.topologicalSort(TENANT, 1L));
        assertEquals(List.of(1L), store.findPath(TENANT, 1L, 1L));
        assertTrue(store.findPath(TENANT, 1L, 99L).isEmpty());
        assertEquals(List.of(2L), store.findMissingPrerequisites(TENANT, 1L, Set.of()));
        assertTrue(store.findMissingPrerequisites(TENANT, 1L, Set.of(2L)).isEmpty());

        store.addNode(TENANT, GraphNode.of(1L, "N1", "n1"));
        store.addEdge(TENANT, 1L, 2L, "PRE", 1D);
        store.removeEdge(TENANT, 1L, 2L, "PRE");
        store.removeNode(TENANT, 1L);
        store.loadAll();
        assertNull(store.getNode(TENANT, 99L));
        verify(knowledge, atLeast(1)).findBySpace(TENANT, 10L);
    }

    @Test
    void handlesKnowledgeWithoutSpaceAndNullTraversalRoot() {
        KnowledgeRepository knowledge = mock(KnowledgeRepository.class);
        KnowledgeRelationRepository relations = mock(KnowledgeRelationRepository.class);
        KnowledgeBO noSpace = new KnowledgeBO(); noSpace.setId(1L); noSpace.setSpaceId(null);
        when(knowledge.findById(TENANT, 1L)).thenReturn(noSpace);
        MemoryGraphStore store = new MemoryGraphStore(knowledge, relations);
        assertNull(store.getNode(TENANT, 1L));
        assertTrue(store.parents(TENANT, 1L).isEmpty());
        assertEquals(List.of(), store.topologicalSort(TENANT, null));
        assertEquals(List.of(), store.dfs(TENANT, null));
    }

    private static KnowledgeBO node(Long id) {
        KnowledgeBO value = new KnowledgeBO(); value.setId(id); value.setSpaceId(10L); value.setName("N" + id); value.setCode("n" + id); return value;
    }

    private static KnowledgeRelationBO relation(Long source, Long target, String type) {
        KnowledgeRelationBO value = new KnowledgeRelationBO(); value.setSpaceId(10L); value.setSourceId(source); value.setTargetId(target); value.setRelationType(type); value.setWeight(1D); return value;
    }
}

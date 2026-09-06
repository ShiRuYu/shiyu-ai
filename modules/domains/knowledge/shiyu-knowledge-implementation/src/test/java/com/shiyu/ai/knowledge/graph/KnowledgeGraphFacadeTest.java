package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KnowledgeGraphFacadeTest {
    @Test
    void delegatesEveryTenantScopedGraphOperation() {
        GraphStore store = mock(GraphStore.class);
        KnowledgeGraph graph = new KnowledgeGraph(store);
        TenantId tenant = new TenantId(7);
        GraphNode node = GraphNode.of(1L, "node", "N");
        GraphEdge edge = new GraphEdge(2L, "PREREQUISITE", 1D);
        when(store.parents(tenant, 1L)).thenReturn(List.of(2L));
        when(store.children(tenant, 1L)).thenReturn(List.of(3L));
        when(store.related(tenant, 1L)).thenReturn(List.of(4L));
        when(store.edges(tenant, 1L)).thenReturn(List.of(edge));
        when(store.topologicalSort(tenant, 1L)).thenReturn(List.of(1L, 2L));
        when(store.dfs(tenant, 1L)).thenReturn(List.of(1L));
        when(store.bfs(tenant, 1L)).thenReturn(List.of(1L));
        when(store.findPath(tenant, 1L, 2L)).thenReturn(List.of(1L, 2L));
        when(store.findMissingPrerequisites(tenant, 1L, Set.of())).thenReturn(List.of(2L));
        when(store.getParentNodes(tenant, 1L)).thenReturn(List.of(node));
        when(store.getChildNodes(tenant, 1L)).thenReturn(List.of(node));
        when(store.getRelatedNodes(tenant, 1L)).thenReturn(List.of(node));

        graph.getNode(tenant, 1L); graph.parents(tenant, 1L); graph.children(tenant, 1L);
        graph.related(tenant, 1L); graph.edges(tenant, 1L); graph.topologicalSort(tenant, 1L);
        graph.dfs(tenant, 1L); graph.bfs(tenant, 1L); graph.findPath(tenant, 1L, 2L);
        graph.findMissingPrerequisites(tenant, 1L, Set.of()); graph.addNode(tenant, node);
        graph.addEdge(tenant, 1L, 2L, "PREREQUISITE", 1D);
        graph.removeEdge(tenant, 1L, 2L, "PREREQUISITE"); graph.removeNode(tenant, 1L);
        graph.getParentNodes(tenant, 1L); graph.getChildNodes(tenant, 1L); graph.getRelatedNodes(tenant, 1L);
        graph.reload();

        verify(store).getNode(tenant, 1L);
        verify(store).addNode(tenant, node);
        verify(store).addEdge(tenant, 1L, 2L, "PREREQUISITE", 1D);
        verify(store).removeEdge(tenant, 1L, 2L, "PREREQUISITE");
        verify(store).removeNode(tenant, 1L);
        verify(store).loadAll();
    }
}

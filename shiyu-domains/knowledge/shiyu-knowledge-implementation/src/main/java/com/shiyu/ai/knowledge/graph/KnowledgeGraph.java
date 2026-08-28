package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class KnowledgeGraph {
    private final GraphStore graphStore;

    public KnowledgeGraph(GraphStore graphStore) {
        this.graphStore = graphStore;
    }

    public GraphNode getNode(TenantId tenantId, Long id) { return graphStore.getNode(tenantId, id); }
    public List<Long> parents(TenantId tenantId, Long id) { return graphStore.parents(tenantId, id); }
    public List<Long> children(TenantId tenantId, Long id) { return graphStore.children(tenantId, id); }
    public List<Long> related(TenantId tenantId, Long id) { return graphStore.related(tenantId, id); }
    public List<GraphEdge> edges(TenantId tenantId, Long id) { return graphStore.edges(tenantId, id); }
    public List<Long> topologicalSort(TenantId tenantId, Long id) { return graphStore.topologicalSort(tenantId, id); }
    public List<Long> dfs(TenantId tenantId, Long id) { return graphStore.dfs(tenantId, id); }
    public List<Long> bfs(TenantId tenantId, Long id) { return graphStore.bfs(tenantId, id); }
    public List<Long> findPath(TenantId tenantId, Long from, Long to) { return graphStore.findPath(tenantId, from, to); }
    public List<Long> findMissingPrerequisites(TenantId tenantId, Long id, Set<Long> masteredIds) {
        return graphStore.findMissingPrerequisites(tenantId, id, masteredIds);
    }
    public void addNode(TenantId tenantId, GraphNode node) { graphStore.addNode(tenantId, node); }
    public void addEdge(TenantId tenantId, Long sourceId, Long targetId, String type, double weight) {
        graphStore.addEdge(tenantId, sourceId, targetId, type, weight);
    }
    public void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type) {
        graphStore.removeEdge(tenantId, sourceId, targetId, type);
    }
    public void removeNode(TenantId tenantId, Long id) { graphStore.removeNode(tenantId, id); }
    public List<GraphNode> getParentNodes(TenantId tenantId, Long id) { return graphStore.getParentNodes(tenantId, id); }
    public List<GraphNode> getChildNodes(TenantId tenantId, Long id) { return graphStore.getChildNodes(tenantId, id); }
    public List<GraphNode> getRelatedNodes(TenantId tenantId, Long id) { return graphStore.getRelatedNodes(tenantId, id); }
    public void reload() { graphStore.loadAll(); }
}

package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;

import java.util.List;
import java.util.Set;

/** Tenant-scoped graph cache facade. */
public interface GraphStore {
    GraphNode getNode(TenantId tenantId, Long id);
    List<Long> parents(TenantId tenantId, Long id);
    List<Long> children(TenantId tenantId, Long id);
    List<Long> related(TenantId tenantId, Long id);
    List<GraphEdge> edges(TenantId tenantId, Long id);
    void addNode(TenantId tenantId, GraphNode node);
    void addEdge(TenantId tenantId, Long sourceId, Long targetId, String type, double weight);
    void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type);
    void removeNode(TenantId tenantId, Long id);
    List<GraphNode> getParentNodes(TenantId tenantId, Long id);
    List<GraphNode> getChildNodes(TenantId tenantId, Long id);
    List<GraphNode> getRelatedNodes(TenantId tenantId, Long id);
    List<Long> topologicalSort(TenantId tenantId, Long rootId);
    List<Long> dfs(TenantId tenantId, Long startId);
    List<Long> bfs(TenantId tenantId, Long startId);
    List<Long> findPath(TenantId tenantId, Long from, Long to);
    List<Long> findMissingPrerequisites(TenantId tenantId, Long targetId, Set<Long> masteredIds);
    void loadAll();
}

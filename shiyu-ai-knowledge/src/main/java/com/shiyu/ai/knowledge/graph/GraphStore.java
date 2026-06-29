package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;

import java.util.List;
import java.util.Set;

public interface GraphStore {

    GraphNode getNode(Long id);

    List<Long> parents(Long id);

    List<Long> children(Long id);

    List<Long> related(Long id);

    List<GraphEdge> edges(Long id);

    void addNode(GraphNode node);

    void addEdge(Long sourceId, Long targetId, String type, double weight);

    void removeEdge(Long sourceId, Long targetId, String type);

    List<Long> topologicalSort(Long rootId);

    List<Long> dfs(Long startId);

    List<Long> bfs(Long startId);

    List<Long> findPath(Long from, Long to);

    List<Long> findMissingPrerequisites(Long targetId, Set<Long> masteredIds);

    void loadAll();
}

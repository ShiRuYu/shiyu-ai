package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.Set;

@Component
public class KnowledgeGraph {

    private final GraphStore graphStore;

    public KnowledgeGraph(GraphStore graphStore) {
        this.graphStore = graphStore;
    }

    public GraphNode getNode(Long id) {
        return graphStore.getNode(id);
    }

    public List<Long> parents(Long id) {
        return graphStore.parents(id);
    }

    public List<Long> children(Long id) {
        return graphStore.children(id);
    }

    public List<Long> related(Long id) {
        return graphStore.related(id);
    }

    public List<GraphEdge> edges(Long id) {
        return graphStore.edges(id);
    }

    public List<Long> topologicalSort(Long rootId) {
        return graphStore.topologicalSort(rootId);
    }

    public List<Long> dfs(Long startId) {
        return graphStore.dfs(startId);
    }

    public List<Long> bfs(Long startId) {
        return graphStore.bfs(startId);
    }

    public List<Long> findPath(Long from, Long to) {
        return graphStore.findPath(from, to);
    }

    public List<Long> findMissingPrerequisites(Long targetId, Set<Long> masteredIds) {
        return graphStore.findMissingPrerequisites(targetId, masteredIds);
    }

    public void addNode(GraphNode node) {
        graphStore.addNode(node);
    }

    public void addEdge(Long sourceId, Long targetId, String type, double weight) {
        graphStore.addEdge(sourceId, targetId, type, weight);
    }

    public void removeEdge(Long sourceId, Long targetId, String type) {
        graphStore.removeEdge(sourceId, targetId, type);
    }



    public void removeNode(Long id) {
        graphStore.removeNode(id);
    }

    public List<GraphNode> getParentNodes(Long id) {
        return graphStore.getParentNodes(id);
    }

    public List<GraphNode> getChildNodes(Long id) {
        return graphStore.getChildNodes(id);
    }

    public List<GraphNode> getRelatedNodes(Long id) {
        return graphStore.getRelatedNodes(id);
    }
    public void reload() {
        graphStore.loadAll();
    }
}


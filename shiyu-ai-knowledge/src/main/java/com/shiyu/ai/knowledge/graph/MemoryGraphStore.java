package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.repository.KnowledgeRelationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(2)
public class MemoryGraphStore implements GraphStore, ApplicationRunner {

    private static final String NODE_PREFIX = "node:";
    private static final String ADJ_PREFIX = "adj:";

    private final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeRelationRepository relationRepository;

    public MemoryGraphStore(KnowledgeRepository knowledgeRepository,
                            KnowledgeRelationRepository relationRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.relationRepository = relationRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        loadAll();
    }

    @Override
    public void loadAll() {
        log.info("开始从数据库加载知识图谱到内存");
        store.clear();

        var allNodes = knowledgeRepository.findAll();
        var allEdges = relationRepository.findAll();

        Map<Long, GraphNode> nodeMap = new HashMap<>();
        for (var k : allNodes) {
            GraphNode node = GraphNode.of(k.getId(), k.getName(), k.getCode());
            nodeMap.put(k.getId(), node);
        }

        for (var e : allEdges) {
            GraphEdge edge = new GraphEdge(e.getTargetId(), e.getRelationType(), e.getWeight());
            GraphNode source = nodeMap.get(e.getSourceId());
            if (source != null) {
                source.getEdges().add(edge);
                switch (e.getRelationType()) {
                    case "PRE" -> {
                        source.getParentIds().add(e.getTargetId());
                        GraphNode target = nodeMap.get(e.getTargetId());
                        if (target != null) {
                            target.getChildIds().add(e.getSourceId());
                        }
                    }
                    case "NEXT" -> source.getChildIds().add(e.getTargetId());
                    case "RELATED", "SIMILAR" -> source.getRelatedIds().add(e.getTargetId());
                    default -> { }
                }
            }
        }

        for (var entry : nodeMap.entrySet()) {
            store.put(NODE_PREFIX + entry.getKey(), JSONUtils.toJsonString(entry.getValue()));
            store.put(ADJ_PREFIX + entry.getKey(), JSONUtils.toJsonString(entry.getValue().getEdges()));
        }

        log.info("知识图谱加载完成: {} 个节点, {} 条边", nodeMap.size(), allEdges.size());
    }

    private String get(String key) {
        return store.get(key);
    }

    private void put(String key, String value) {
        store.put(key, value);
    }

    private void remove(String key) {
        store.remove(key);
    }

    @Override
    public GraphNode getNode(Long id) {
        String json = get(NODE_PREFIX + id);
        if (json == null) return null;
        return JSONUtils.parseObject(json, GraphNode.class);
    }

    @Override
    public List<Long> parents(Long id) {
        GraphNode node = getNode(id);
        return node != null ? node.getParentIds() : Collections.emptyList();
    }

    @Override
    public List<Long> children(Long id) {
        GraphNode node = getNode(id);
        return node != null ? node.getChildIds() : Collections.emptyList();
    }

    @Override
    public List<Long> related(Long id) {
        GraphNode node = getNode(id);
        return node != null ? node.getRelatedIds() : Collections.emptyList();
    }

    @Override
    public List<GraphNode> getParentNodes(Long id) {
        return parents(id).stream()
                .map(this::getNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<GraphNode> getChildNodes(Long id) {
        return children(id).stream()
                .map(this::getNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<GraphNode> getRelatedNodes(Long id) {
        return related(id).stream()
                .map(this::getNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<GraphEdge> edges(Long id) {
        String json = get(ADJ_PREFIX + id);
        if (json == null) return Collections.emptyList();
        return JSONUtils.parseArray(json, GraphEdge.class);
    }

    @Override
    public void addNode(GraphNode node) {
        put(NODE_PREFIX + node.getId(), JSONUtils.toJsonString(node));
        put(ADJ_PREFIX + node.getId(), JSONUtils.toJsonString(node.getEdges()));
    }

    @Override
    public void addEdge(Long sourceId, Long targetId, String type, double weight) {
        GraphNode source = getNode(sourceId);
        if (source == null) return;
        GraphEdge edge = new GraphEdge(targetId, type, weight);
        source.getEdges().add(edge);

        switch (type) {
            case "PRE" -> source.getParentIds().add(targetId);
            case "NEXT" -> source.getChildIds().add(targetId);
            case "RELATED", "SIMILAR" -> source.getRelatedIds().add(targetId);
            default -> { }
        }

        addNode(source);

        if ("PRE".equals(type)) {
            GraphNode target = getNode(targetId);
            if (target != null) {
                target.getChildIds().add(sourceId);
                addNode(target);
            }
        }
    }

    @Override
    public void removeEdge(Long sourceId, Long targetId, String type) {
        GraphNode source = getNode(sourceId);
        if (source == null) return;
        source.getEdges().removeIf(e -> e.getTargetId().equals(targetId) && e.getType().equals(type));

        switch (type) {
            case "PRE" -> source.getParentIds().remove(targetId);
            case "NEXT" -> source.getChildIds().remove(targetId);
            case "RELATED", "SIMILAR" -> source.getRelatedIds().remove(targetId);
            default -> { }
        }
        addNode(source);
    }

    @Override
    public void removeNode(Long id) {
        remove(NODE_PREFIX + id);
        remove(ADJ_PREFIX + id);
        log.info("Removed graph node: id={}", id);
    }

    @Override
    public List<Long> topologicalSort(Long rootId) {
        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        dfsVisit(rootId, visited, result);
        Collections.reverse(result);
        return result;
    }

    @Override
    public List<Long> dfs(Long startId) {
        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        dfsVisit(startId, visited, result);
        return result;
    }

    private void dfsVisit(Long id, Set<Long> visited, List<Long> result) {
        if (id == null || visited.contains(id)) return;
        visited.add(id);
        for (Long parentId : parents(id)) {
            dfsVisit(parentId, visited, result);
        }
        result.add(id);
    }

    @Override
    public List<Long> bfs(Long startId) {
        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            result.add(current);
            for (Long parentId : parents(current)) {
                if (!visited.contains(parentId)) {
                    visited.add(parentId);
                    queue.offer(parentId);
                }
            }
        }
        return result;
    }

    @Override
    public List<Long> findPath(Long from, Long to) {
        Map<Long, Long> parentMap = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.offer(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(to)) {
                return reconstructPath(parentMap, from, to);
            }
            for (Long childId : children(current)) {
                if (!visited.contains(childId)) {
                    visited.add(childId);
                    parentMap.put(childId, current);
                    queue.offer(childId);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Long> reconstructPath(Map<Long, Long> parentMap, Long from, Long to) {
        List<Long> path = new ArrayList<>();
        Long current = to;
        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public List<Long> findMissingPrerequisites(Long targetId, Set<Long> masteredIds) {
        List<Long> all = topologicalSort(targetId);
        return all.stream()
                .filter(id -> !masteredIds.contains(id))
                .filter(id -> !id.equals(targetId))
                .collect(Collectors.toList());
    }
}

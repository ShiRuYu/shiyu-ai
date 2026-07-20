package com.shiyu.ai.knowledge.graph;

import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeRelationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存图存储 — 基于 ConcurrentHashMap 对象存储
 *
 * <p>使用 {@code Map<Long, GraphNode>} 直接存储节点对象，
 * 避免 JSON 序列化/反序列化带来的性能和类型安全问题。</p>
 */
@Slf4j
@Component
@Order(2)
public class MemoryGraphStore implements GraphStore, ApplicationRunner {

    private final ConcurrentHashMap<Long, GraphNode> nodeStore = new ConcurrentHashMap<>();
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
        nodeStore.clear();

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
                    case "INCLUDE" -> source.getParentIds().add(e.getTargetId());
                    case "BELONG" -> source.getRelatedIds().add(e.getTargetId());
                    default -> { }
                }
            }
        }

        nodeStore.putAll(nodeMap);
        log.info("知识图谱加载完成: {} 个节点, {} 条边", nodeMap.size(), allEdges.size());
    }

    @Override
    public GraphNode getNode(Long id) {
        return nodeStore.get(id);
    }

    @Override
    public List<Long> parents(Long id) {
        GraphNode node = nodeStore.get(id);
        return node != null ? node.getParentIds() : Collections.emptyList();
    }

    @Override
    public List<Long> children(Long id) {
        GraphNode node = nodeStore.get(id);
        return node != null ? node.getChildIds() : Collections.emptyList();
    }

    @Override
    public List<Long> related(Long id) {
        GraphNode node = nodeStore.get(id);
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
        GraphNode node = nodeStore.get(id);
        return node != null ? node.getEdges() : Collections.emptyList();
    }

    @Override
    public void addNode(GraphNode node) {
        nodeStore.put(node.getId(), node);
    }

    @Override
    public void addEdge(Long sourceId, Long targetId, String type, double weight) {
        GraphNode source = nodeStore.get(sourceId);
        if (source == null) {
            log.warn("addEdge: 源节点 {} 不存在", sourceId);
            return;
        }
        GraphEdge edge = new GraphEdge(targetId, type, weight);
        source.getEdges().add(edge);

        switch (type) {
            case "PRE" -> source.getParentIds().add(targetId);
            case "NEXT" -> source.getChildIds().add(targetId);
            case "RELATED", "SIMILAR" -> source.getRelatedIds().add(targetId);
            default -> { }
        }

        if ("PRE".equals(type)) {
            GraphNode target = nodeStore.get(targetId);
            if (target != null) {
                target.getChildIds().add(sourceId);
            }
        }
    }

    @Override
    public void removeEdge(Long sourceId, Long targetId, String type) {
        GraphNode source = nodeStore.get(sourceId);
        if (source == null) return;
        source.getEdges().removeIf(e -> e.getTargetId().equals(targetId) && e.getType().equals(type));

        switch (type) {
            case "PRE" -> source.getParentIds().remove(targetId);
            case "NEXT" -> source.getChildIds().remove(targetId);
            case "RELATED", "SIMILAR" -> source.getRelatedIds().remove(targetId);
            default -> { }
        }
    }

    @Override
    public void removeNode(Long id) {
        nodeStore.remove(id);
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

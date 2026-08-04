package com.shiyu.ai.knowledge.graph;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

/** Tenant/space isolated lazy graph cache. H2 remains the source of truth. */
@Component
public class MemoryGraphStore implements GraphStore {

    private final KnowledgeRepository knowledgeRepository;
    private final KnowledgeRelationRepository relationRepository;
    private final Cache<GraphKey, Map<Long, GraphNode>> cache = Caffeine.newBuilder()
            .maximumSize(100).expireAfterAccess(Duration.ofMinutes(15)).build();

    public MemoryGraphStore(KnowledgeRepository knowledgeRepository,
                            KnowledgeRelationRepository relationRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.relationRepository = relationRepository;
    }

    @Override public GraphNode getNode(Long id) { return graphForNode(id).get(id); }
    @Override public List<Long> parents(Long id) { return ids(id, NodeList.PARENTS); }
    @Override public List<Long> children(Long id) { return ids(id, NodeList.CHILDREN); }
    @Override public List<Long> related(Long id) { return ids(id, NodeList.RELATED); }
    @Override public List<GraphEdge> edges(Long id) {
        GraphNode node = getNode(id);
        return node == null ? List.of() : List.copyOf(node.getEdges());
    }
    @Override public void addNode(GraphNode node) { invalidate(node.getId()); }
    @Override public void addEdge(Long sourceId, Long targetId, String type, double weight) { invalidate(sourceId); }
    @Override public void removeEdge(Long sourceId, Long targetId, String type) { invalidate(sourceId); }
    @Override public void removeNode(Long id) { invalidate(id); }
    @Override public List<GraphNode> getParentNodes(Long id) { return nodes(parents(id)); }
    @Override public List<GraphNode> getChildNodes(Long id) { return nodes(children(id)); }
    @Override public List<GraphNode> getRelatedNodes(Long id) { return nodes(related(id)); }

    @Override
    public List<Long> topologicalSort(Long rootId) {
        List<Long> result = new ArrayList<>();
        visit(rootId, new HashSet<>(), result);
        Collections.reverse(result);
        return result;
    }

    @Override
    public List<Long> dfs(Long startId) {
        List<Long> result = new ArrayList<>();
        visit(startId, new HashSet<>(), result);
        return result;
    }

    @Override
    public List<Long> bfs(Long startId) {
        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(startId);
        visited.add(startId);
        while (!queue.isEmpty()) {
            Long current = queue.remove();
            result.add(current);
            for (Long parent : parents(current)) if (visited.add(parent)) queue.add(parent);
        }
        return result;
    }

    @Override
    public List<Long> findPath(Long from, Long to) {
        Map<Long, Long> previous = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(from);
        visited.add(from);
        while (!queue.isEmpty()) {
            Long current = queue.remove();
            if (Objects.equals(current, to)) {
                List<Long> path = new ArrayList<>();
                for (Long value = to; value != null; value = previous.get(value)) path.add(value);
                Collections.reverse(path);
                return path;
            }
            for (Long child : children(current)) {
                if (visited.add(child)) {
                    previous.put(child, current);
                    queue.add(child);
                }
            }
        }
        return List.of();
    }

    @Override
    public List<Long> findMissingPrerequisites(Long targetId, Set<Long> masteredIds) {
        return topologicalSort(targetId).stream()
                .filter(id -> !Objects.equals(id, targetId))
                .filter(id -> !masteredIds.contains(id)).toList();
    }

    @Override public void loadAll() { cache.invalidateAll(); }

    private List<Long> ids(Long id, NodeList type) {
        GraphNode node = getNode(id);
        if (node == null) return List.of();
        return switch (type) {
            case PARENTS -> List.copyOf(node.getParentIds());
            case CHILDREN -> List.copyOf(node.getChildIds());
            case RELATED -> List.copyOf(node.getRelatedIds());
        };
    }

    private List<GraphNode> nodes(List<Long> ids) {
        return ids.stream().map(this::getNode).filter(Objects::nonNull).toList();
    }

    private void visit(Long id, Set<Long> visited, List<Long> result) {
        if (id == null || !visited.add(id)) return;
        for (Long parentId : parents(id)) visit(parentId, visited, result);
        result.add(id);
    }

    private Map<Long, GraphNode> graphForNode(Long nodeId) {
        KnowledgeBO knowledge = knowledgeRepository.findById(nodeId);
        if (knowledge == null || knowledge.getSpaceId() == null) return Map.of();
        return cache.get(new GraphKey(knowledge.getTenantId(), knowledge.getSpaceId()),
                ignored -> load(knowledge.getSpaceId()));
    }

    private Map<Long, GraphNode> load(Long spaceId) {
        Map<Long, GraphNode> nodes = new HashMap<>();
        for (KnowledgeBO knowledge : knowledgeRepository.findBySpace(spaceId)) {
            nodes.put(knowledge.getId(),
                    GraphNode.of(knowledge.getId(), knowledge.getName(), knowledge.getCode()));
        }
        relationRepository.findBySpace(spaceId).forEach(relation -> {
            GraphNode source = nodes.get(relation.getSourceId());
            if (source == null) return;
            source.getEdges().add(new GraphEdge(relation.getTargetId(),
                    relation.getRelationType(), relation.getWeight()));
            switch (relation.getRelationType()) {
                case "PRE" -> {
                    source.getParentIds().add(relation.getTargetId());
                    GraphNode target = nodes.get(relation.getTargetId());
                    if (target != null) target.getChildIds().add(relation.getSourceId());
                }
                case "NEXT" -> source.getChildIds().add(relation.getTargetId());
                case "RELATED", "SIMILAR", "BELONG" -> source.getRelatedIds().add(relation.getTargetId());
                case "INCLUDE" -> source.getParentIds().add(relation.getTargetId());
                default -> { }
            }
        });
        return nodes;
    }

    private void invalidate(Long nodeId) {
        KnowledgeBO knowledge = knowledgeRepository.findById(nodeId);
        if (knowledge != null) cache.invalidate(
                new GraphKey(knowledge.getTenantId(), knowledge.getSpaceId()));
    }

    private enum NodeList { PARENTS, CHILDREN, RELATED }
    private record GraphKey(Long tenantId, Long spaceId) { }
}

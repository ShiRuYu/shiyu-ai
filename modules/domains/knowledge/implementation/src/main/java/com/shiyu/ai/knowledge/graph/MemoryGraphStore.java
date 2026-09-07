package com.shiyu.ai.knowledge.graph;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.domain.GraphEdge;
import com.shiyu.ai.knowledge.domain.GraphNode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeRepository;
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

    @Override public GraphNode getNode(TenantId tenantId, Long id) { return graphForNode(tenantId, id).get(id); }
    @Override public List<Long> parents(TenantId tenantId, Long id) { return ids(tenantId, id, NodeList.PARENTS); }
    @Override public List<Long> children(TenantId tenantId, Long id) { return ids(tenantId, id, NodeList.CHILDREN); }
    @Override public List<Long> related(TenantId tenantId, Long id) { return ids(tenantId, id, NodeList.RELATED); }
    @Override public List<GraphEdge> edges(TenantId tenantId, Long id) {
        GraphNode node = getNode(tenantId, id);
        return node == null ? List.of() : List.copyOf(node.getEdges());
    }
    @Override public void addNode(TenantId tenantId, GraphNode node) { invalidate(tenantId, node.getId()); }
    @Override public void addEdge(TenantId tenantId, Long sourceId, Long targetId, String type, double weight) { invalidate(tenantId, sourceId); }
    @Override public void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type) { invalidate(tenantId, sourceId); }
    @Override public void removeNode(TenantId tenantId, Long id) { invalidate(tenantId, id); }
    @Override public List<GraphNode> getParentNodes(TenantId tenantId, Long id) { return nodes(tenantId, parents(tenantId, id)); }
    @Override public List<GraphNode> getChildNodes(TenantId tenantId, Long id) { return nodes(tenantId, children(tenantId, id)); }
    @Override public List<GraphNode> getRelatedNodes(TenantId tenantId, Long id) { return nodes(tenantId, related(tenantId, id)); }

    @Override
    public List<Long> topologicalSort(TenantId tenantId, Long rootId) {
        List<Long> result = new ArrayList<>();
        visit(tenantId, rootId, new HashSet<>(), result);
        Collections.reverse(result);
        return result;
    }

    @Override
    public List<Long> dfs(TenantId tenantId, Long startId) {
        List<Long> result = new ArrayList<>();
        visit(tenantId, startId, new HashSet<>(), result);
        return result;
    }

    @Override
    public List<Long> bfs(TenantId tenantId, Long startId) {
        List<Long> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(startId);
        visited.add(startId);
        while (!queue.isEmpty()) {
            Long current = queue.remove();
            result.add(current);
            for (Long parent : parents(tenantId, current)) if (visited.add(parent)) queue.add(parent);
        }
        return result;
    }

    @Override
    public List<Long> findPath(TenantId tenantId, Long from, Long to) {
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
            for (Long child : children(tenantId, current)) {
                if (visited.add(child)) {
                    previous.put(child, current);
                    queue.add(child);
                }
            }
        }
        return List.of();
    }

    @Override
    public List<Long> findMissingPrerequisites(TenantId tenantId, Long targetId, Set<Long> masteredIds) {
        return topologicalSort(tenantId, targetId).stream()
                .filter(id -> !Objects.equals(id, targetId))
                .filter(id -> !masteredIds.contains(id)).toList();
    }

    @Override public void loadAll() { cache.invalidateAll(); }

    private List<Long> ids(TenantId tenantId, Long id, NodeList type) {
        GraphNode node = getNode(tenantId, id);
        if (node == null) return List.of();
        return switch (type) {
            case PARENTS -> List.copyOf(node.getParentIds());
            case CHILDREN -> List.copyOf(node.getChildIds());
            case RELATED -> List.copyOf(node.getRelatedIds());
        };
    }

    private List<GraphNode> nodes(TenantId tenantId, List<Long> ids) {
        return ids.stream().map(id -> getNode(tenantId, id)).filter(Objects::nonNull).toList();
    }

    private void visit(TenantId tenantId, Long id, Set<Long> visited, List<Long> result) {
        if (id == null || !visited.add(id)) return;
        for (Long parentId : parents(tenantId, id)) visit(tenantId, parentId, visited, result);
        result.add(id);
    }

    private Map<Long, GraphNode> graphForNode(TenantId tenantId, Long nodeId) {
        KnowledgeBO knowledge = knowledgeRepository.findById(tenantId, nodeId);
        if (knowledge == null || knowledge.getSpaceId() == null) return Map.of();
        return cache.get(new GraphKey(tenantId.value(), knowledge.getSpaceId()),
                ignored -> load(tenantId, knowledge.getSpaceId()));
    }

    private Map<Long, GraphNode> load(TenantId tenantId, Long spaceId) {
        Map<Long, GraphNode> nodes = new HashMap<>();
        for (KnowledgeBO knowledge : knowledgeRepository.findBySpace(tenantId, spaceId)) {
            nodes.put(knowledge.getId(), GraphNode.of(knowledge.getId(), knowledge.getName(), knowledge.getCode()));
        }
        relationRepository.findBySpace(tenantId, spaceId).forEach(relation -> {
            GraphNode source = nodes.get(relation.getSourceId());
            if (source == null) return;
            source.getEdges().add(new GraphEdge(relation.getTargetId(), relation.getRelationType(), relation.getWeight()));
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

    private void invalidate(TenantId tenantId, Long nodeId) {
        KnowledgeBO knowledge = knowledgeRepository.findById(tenantId, nodeId);
        if (knowledge != null) cache.invalidate(new GraphKey(tenantId.value(), knowledge.getSpaceId()));
    }

    private enum NodeList { PARENTS, CHILDREN, RELATED }
    private record GraphKey(Long tenantId, Long spaceId) { }
}

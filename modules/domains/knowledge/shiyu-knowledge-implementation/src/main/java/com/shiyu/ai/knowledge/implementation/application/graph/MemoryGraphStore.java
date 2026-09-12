package com.shiyu.ai.knowledge.implementation.application.graph;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.GraphEdge;
import com.shiyu.ai.knowledge.implementation.domain.GraphNode;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRelationRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeRepository;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

/**
 * 管理知识图谱节点和边的保存、查询与删除。
 */
@Component
public class MemoryGraphStore implements GraphStore {
    /**
     * knowledgeRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRepository knowledgeRepository;
    /**
     * relationRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeRelationRepository relationRepository;
    private final Cache<GraphKey, Map<Long, GraphNode>> cache =
            Caffeine.newBuilder()
                    .maximumSize(100)
                    .expireAfterAccess(Duration.ofMinutes(15))
                    .build();

    /**
     * {@code MemoryGraphStore} 创建并初始化当前类型实例。
     *
     * @param knowledgeRepository 参数值，用于执行当前操作。
     * @param relationRepository 参数值，用于执行当前操作。
     */
    public MemoryGraphStore(
            KnowledgeRepository knowledgeRepository,
            KnowledgeRelationRepository relationRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.relationRepository = relationRepository;
    }

    /**
     * {@code getNode} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public GraphNode getNode(TenantId tenantId, Long id) {
        return graphForNode(tenantId, id).get(id);
    }

    /**
     * {@code parents} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> parents(TenantId tenantId, Long id) {
        return ids(tenantId, id, NodeList.PARENTS);
    }

    /**
     * {@code children} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> children(TenantId tenantId, Long id) {
        return ids(tenantId, id, NodeList.CHILDREN);
    }

    /**
     * {@code related} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> related(TenantId tenantId, Long id) {
        return ids(tenantId, id, NodeList.RELATED);
    }

    /**
     * {@code edges} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<GraphEdge> edges(TenantId tenantId, Long id) {
        GraphNode node = getNode(tenantId, id);
        return node == null ? List.of() : List.copyOf(node.getEdges());
    }

    /**
     * {@code addNode} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param node 参数值，用于执行当前操作。
     */
    @Override
    public void addNode(TenantId tenantId, GraphNode node) {
        invalidate(tenantId, node.getId());
    }

    /**
     * {@code addEdge} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     * @param weight 参数值，用于执行当前操作。
     */
    @Override
    public void addEdge(
            TenantId tenantId, Long sourceId, Long targetId, String type, double weight) {
        invalidate(tenantId, sourceId);
    }

    /**
     * {@code removeEdge} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     */
    @Override
    public void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type) {
        invalidate(tenantId, sourceId);
    }

    /**
     * {@code removeNode} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    @Override
    public void removeNode(TenantId tenantId, Long id) {
        invalidate(tenantId, id);
    }

    /**
     * {@code getParentNodes} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<GraphNode> getParentNodes(TenantId tenantId, Long id) {
        return nodes(tenantId, parents(tenantId, id));
    }

    /**
     * {@code getChildNodes} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<GraphNode> getChildNodes(TenantId tenantId, Long id) {
        return nodes(tenantId, children(tenantId, id));
    }

    /**
     * {@code getRelatedNodes} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<GraphNode> getRelatedNodes(TenantId tenantId, Long id) {
        return nodes(tenantId, related(tenantId, id));
    }

    /**
     * {@code topologicalSort} 将当前对象转换为目标表示形式。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param rootId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> topologicalSort(TenantId tenantId, Long rootId) {
        List<Long> result = new ArrayList<>();
        visit(tenantId, rootId, new HashSet<>(), result);
        Collections.reverse(result);
        return result;
    }

    /**
     * {@code dfs} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param startId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> dfs(TenantId tenantId, Long startId) {
        List<Long> result = new ArrayList<>();
        visit(tenantId, startId, new HashSet<>(), result);
        return result;
    }

    /**
     * {@code bfs} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param startId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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
            for (Long parent : parents(tenantId, current))
                if (visited.add(parent)) queue.add(parent);
        }
        return result;
    }

    /**
     * {@code findPath} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param from 参数值，用于执行当前操作。
     * @param to 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code findMissingPrerequisites} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param masteredIds 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Long> findMissingPrerequisites(
            TenantId tenantId, Long targetId, Set<Long> masteredIds) {
        return topologicalSort(tenantId, targetId).stream()
                .filter(id -> !Objects.equals(id, targetId))
                .filter(id -> !masteredIds.contains(id))
                .toList();
    }

    /**
     * {@code loadAll} 查询并返回当前操作所需的数据。
     */
    @Override
    public void loadAll() {
        cache.invalidateAll();
    }

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
        return cache.get(
                new GraphKey(tenantId.value(), knowledge.getSpaceId()),
                ignored -> load(tenantId, knowledge.getSpaceId()));
    }

    private Map<Long, GraphNode> load(TenantId tenantId, Long spaceId) {
        Map<Long, GraphNode> nodes = new HashMap<>();
        for (KnowledgeBO knowledge : knowledgeRepository.findBySpace(tenantId, spaceId)) {
            nodes.put(
                    knowledge.getId(),
                    GraphNode.of(knowledge.getId(), knowledge.getName(), knowledge.getCode()));
        }
        relationRepository
                .findBySpace(tenantId, spaceId)
                .forEach(
                        relation -> {
                            GraphNode source = nodes.get(relation.getSourceId());
                            if (source == null) return;
                            source.getEdges()
                                    .add(
                                            new GraphEdge(
                                                    relation.getTargetId(),
                                                    relation.getRelationType(),
                                                    relation.getWeight()));
                            switch (relation.getRelationType()) {
                                case "PRE" -> {
                                    source.getParentIds().add(relation.getTargetId());
                                    GraphNode target = nodes.get(relation.getTargetId());
                                    if (target != null)
                                        target.getChildIds().add(relation.getSourceId());
                                }
                                case "NEXT" -> source.getChildIds().add(relation.getTargetId());
                                case "RELATED", "SIMILAR", "BELONG" ->
                                        source.getRelatedIds().add(relation.getTargetId());
                                case "INCLUDE" -> source.getParentIds().add(relation.getTargetId());
                                default -> {}
                            }
                        });
        return nodes;
    }

    private void invalidate(TenantId tenantId, Long nodeId) {
        KnowledgeBO knowledge = knowledgeRepository.findById(tenantId, nodeId);
        if (knowledge != null)
            cache.invalidate(new GraphKey(tenantId.value(), knowledge.getSpaceId()));
    }

    /**
     * {@code NodeList} 表示知识模块中的一组受控业务状态或分类。
     */
    private enum NodeList {
        PARENTS,
        CHILDREN,
        RELATED
    }

    /**
     * {@code GraphKey} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     */
    private record GraphKey(Long tenantId, Long spaceId) {}
}

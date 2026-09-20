package com.shiyu.ai.knowledge.implementation.infrastructure.graph;

import com.shiyu.ai.knowledge.implementation.application.port.graph.GraphStore;

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
 * 管理 记忆 Graph 相关的运行时状态、注册信息或临时数据。
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
     * 执行 记忆 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param knowledgeRepository 用于完成本次业务处理的 knowledgeRepository 参数。
     * @param relationRepository 用于完成本次业务处理的 relationRepository 参数。
     */
    public MemoryGraphStore(
            KnowledgeRepository knowledgeRepository,
            KnowledgeRelationRepository relationRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.relationRepository = relationRepository;
    }

    /**
     * 查询 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 记忆 Graph 相关操作生成的结果数据。
     */
    @Override
    public GraphNode getNode(TenantId tenantId, Long id) {
        return graphForNode(tenantId, id).get(id);
    }

    /**
     * 执行 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> parents(TenantId tenantId, Long id) {
        return ids(tenantId, id, NodeList.PARENTS);
    }

    /**
     * 执行 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> children(TenantId tenantId, Long id) {
        return ids(tenantId, id, NodeList.CHILDREN);
    }

    /**
     * 执行 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> related(TenantId tenantId, Long id) {
        return ids(tenantId, id, NodeList.RELATED);
    }

    /**
     * 执行 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<GraphEdge> edges(TenantId tenantId, Long id) {
        GraphNode node = getNode(tenantId, id);
        return node == null ? List.of() : List.copyOf(node.getEdges());
    }

    /**
     * 创建或保存 记忆 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param node 用于完成本次业务处理的 node 参数。
     */
    @Override
    public void addNode(TenantId tenantId, GraphNode node) {
        invalidate(tenantId, node.getId());
    }

    /**
     * 创建或保存 记忆 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param weight 用于完成本次业务处理的 weight 参数。
     */
    @Override
    public void addEdge(
            TenantId tenantId, Long sourceId, Long targetId, String type, double weight) {
        invalidate(tenantId, sourceId);
    }

    /**
     * 删除或移除 记忆 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     */
    @Override
    public void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type) {
        invalidate(tenantId, sourceId);
    }

    /**
     * 删除或移除 记忆 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     */
    @Override
    public void removeNode(TenantId tenantId, Long id) {
        invalidate(tenantId, id);
    }

    /**
     * 查询 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<GraphNode> getParentNodes(TenantId tenantId, Long id) {
        return nodes(tenantId, parents(tenantId, id));
    }

    /**
     * 查询 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<GraphNode> getChildNodes(TenantId tenantId, Long id) {
        return nodes(tenantId, children(tenantId, id));
    }

    /**
     * 查询 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<GraphNode> getRelatedNodes(TenantId tenantId, Long id) {
        return nodes(tenantId, related(tenantId, id));
    }

    /**
     * 构建或转换 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param rootId 用于定位root的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> topologicalSort(TenantId tenantId, Long rootId) {
        List<Long> result = new ArrayList<>();
        visit(tenantId, rootId, new HashSet<>(), result);
        Collections.reverse(result);
        return result;
    }

    /**
     * 执行 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param startId 用于定位start的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Long> dfs(TenantId tenantId, Long startId) {
        List<Long> result = new ArrayList<>();
        visit(tenantId, startId, new HashSet<>(), result);
        return result;
    }

    /**
     * 执行 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param startId 用于定位start的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param from 用于完成本次业务处理的 from 参数。
     * @param to 用于完成本次业务处理的 to 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 查询 记忆 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param targetId 用于定位target的标识。
     * @param masteredIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 定义 Node List 可用的枚举值及其业务语义。
     */
    private enum NodeList {
        PARENTS,
        CHILDREN,
        RELATED
    }

    /**
     * 封装 Graph Key 相关的不可变数据及其字段约束。
     */
    private record GraphKey(Long tenantId, Long spaceId) {}
}

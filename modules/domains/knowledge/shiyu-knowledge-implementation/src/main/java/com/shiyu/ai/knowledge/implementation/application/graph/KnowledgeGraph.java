package com.shiyu.ai.knowledge.implementation.application.graph;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.GraphEdge;
import com.shiyu.ai.knowledge.implementation.domain.GraphNode;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * {@code KnowledgeGraph} 承载知识模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
public class KnowledgeGraph {
    /**
     * 图结构存储，表示当前对象中的对应属性。
     */
    private final GraphStore graphStore;

    /**
     * {@code KnowledgeGraph} 创建并初始化当前类型实例。
     *
     * @param graphStore 参数值，用于执行当前操作。
     */
    public KnowledgeGraph(GraphStore graphStore) {
        this.graphStore = graphStore;
    }

    /**
     * {@code getNode} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public GraphNode getNode(TenantId tenantId, Long id) {
        return graphStore.getNode(tenantId, id);
    }

    /**
     * {@code parents} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> parents(TenantId tenantId, Long id) {
        return graphStore.parents(tenantId, id);
    }

    /**
     * {@code children} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> children(TenantId tenantId, Long id) {
        return graphStore.children(tenantId, id);
    }

    /**
     * {@code related} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> related(TenantId tenantId, Long id) {
        return graphStore.related(tenantId, id);
    }

    /**
     * {@code edges} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<GraphEdge> edges(TenantId tenantId, Long id) {
        return graphStore.edges(tenantId, id);
    }

    /**
     * {@code topologicalSort} 将当前对象转换为目标表示形式。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> topologicalSort(TenantId tenantId, Long id) {
        return graphStore.topologicalSort(tenantId, id);
    }

    /**
     * {@code dfs} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> dfs(TenantId tenantId, Long id) {
        return graphStore.dfs(tenantId, id);
    }

    /**
     * {@code bfs} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> bfs(TenantId tenantId, Long id) {
        return graphStore.bfs(tenantId, id);
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
    public List<Long> findPath(TenantId tenantId, Long from, Long to) {
        return graphStore.findPath(tenantId, from, to);
    }

    /**
     * {@code findMissingPrerequisites} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param masteredIds 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<Long> findMissingPrerequisites(TenantId tenantId, Long id, Set<Long> masteredIds) {
        return graphStore.findMissingPrerequisites(tenantId, id, masteredIds);
    }

    /**
     * {@code addNode} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param node 参数值，用于执行当前操作。
     */
    public void addNode(TenantId tenantId, GraphNode node) {
        graphStore.addNode(tenantId, node);
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
    public void addEdge(
            TenantId tenantId, Long sourceId, Long targetId, String type, double weight) {
        graphStore.addEdge(tenantId, sourceId, targetId, type, weight);
    }

    /**
     * {@code removeEdge} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param targetId 参数值，用于执行当前操作。
     * @param type 参数值，用于执行当前操作。
     */
    public void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type) {
        graphStore.removeEdge(tenantId, sourceId, targetId, type);
    }

    /**
     * {@code removeNode} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    public void removeNode(TenantId tenantId, Long id) {
        graphStore.removeNode(tenantId, id);
    }

    /**
     * {@code getParentNodes} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<GraphNode> getParentNodes(TenantId tenantId, Long id) {
        return graphStore.getParentNodes(tenantId, id);
    }

    /**
     * {@code getChildNodes} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<GraphNode> getChildNodes(TenantId tenantId, Long id) {
        return graphStore.getChildNodes(tenantId, id);
    }

    /**
     * {@code getRelatedNodes} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<GraphNode> getRelatedNodes(TenantId tenantId, Long id) {
        return graphStore.getRelatedNodes(tenantId, id);
    }

    /**
     * {@code reload} 执行当前类型定义的业务操作。
     */
    public void reload() {
        graphStore.loadAll();
    }
}

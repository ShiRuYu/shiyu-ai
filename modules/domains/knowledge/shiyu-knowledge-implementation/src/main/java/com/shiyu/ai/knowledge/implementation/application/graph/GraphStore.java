package com.shiyu.ai.knowledge.implementation.application.graph;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.GraphEdge;
import com.shiyu.ai.knowledge.implementation.domain.GraphNode;

import java.util.List;
import java.util.Set;

/**
 * GraphStore 接口，定义知识模块的能力边界。
 */
public interface GraphStore {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 操作结果。
     */
    GraphNode getNode(TenantId tenantId, Long id);

    /**
     * 执行 {@code parents} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> parents(TenantId tenantId, Long id);

    /**
     * 执行 {@code children} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> children(TenantId tenantId, Long id);

    /**
     * 执行 {@code related} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> related(TenantId tenantId, Long id);

    /**
     * 执行 {@code edges} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<GraphEdge> edges(TenantId tenantId, Long id);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param node 方法参数。
     */
    void addNode(TenantId tenantId, GraphNode node);

    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param sourceId 方法参数。
     * @param targetId 方法参数。
     * @param type 对象类型。
     * @param weight 方法参数。
     */
    void addEdge(TenantId tenantId, Long sourceId, Long targetId, String type, double weight);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param sourceId 方法参数。
     * @param targetId 方法参数。
     * @param type 对象类型。
     */
    void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     */
    void removeNode(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<GraphNode> getParentNodes(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<GraphNode> getChildNodes(TenantId tenantId, Long id);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param id 目标对象标识。
     *
     * @return 符合条件的结果集合。
     */
    List<GraphNode> getRelatedNodes(TenantId tenantId, Long id);

    /**
     * 执行 {@code topologicalSort} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param rootId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> topologicalSort(TenantId tenantId, Long rootId);

    /**
     * 执行 {@code dfs} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param startId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> dfs(TenantId tenantId, Long startId);

    /**
     * 执行 {@code bfs} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param startId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> bfs(TenantId tenantId, Long startId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param from 方法参数。
     * @param to 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> findPath(TenantId tenantId, Long from, Long to);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param targetId 方法参数。
     * @param masteredIds 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Long> findMissingPrerequisites(TenantId tenantId, Long targetId, Set<Long> masteredIds);

    /**
     * 根据条件查询并返回所需数据。
     */
    void loadAll();
}

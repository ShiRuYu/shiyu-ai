package com.shiyu.ai.knowledge.implementation.application.port.graph;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.GraphEdge;
import com.shiyu.ai.knowledge.implementation.domain.GraphNode;

import java.util.List;
import java.util.Set;

/**
 * 管理 Graph 相关的运行时状态、注册信息或临时数据。
 */
public interface GraphStore {
    /**
     * 查询 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 Graph 相关操作生成的结果数据。
     */
    GraphNode getNode(TenantId tenantId, Long id);

    /**
     * 执行 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> parents(TenantId tenantId, Long id);

    /**
     * 执行 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> children(TenantId tenantId, Long id);

    /**
     * 执行 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> related(TenantId tenantId, Long id);

    /**
     * 执行 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<GraphEdge> edges(TenantId tenantId, Long id);

    /**
     * 创建或保存 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param node 用于完成本次业务处理的 node 参数。
     */
    void addNode(TenantId tenantId, GraphNode node);

    /**
     * 创建或保存 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param weight 用于完成本次业务处理的 weight 参数。
     */
    void addEdge(TenantId tenantId, Long sourceId, Long targetId, String type, double weight);

    /**
     * 删除或移除 Graph 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
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
     * 构建或转换 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param rootId 用于定位root的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> topologicalSort(TenantId tenantId, Long rootId);

    /**
     * 执行 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param startId 用于定位start的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> dfs(TenantId tenantId, Long startId);

    /**
     * 执行 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param startId 用于定位start的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> bfs(TenantId tenantId, Long startId);

    /**
     * 查询 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param from 用于完成本次业务处理的 from 参数。
     * @param to 用于完成本次业务处理的 to 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> findPath(TenantId tenantId, Long from, Long to);

    /**
     * 查询 Graph 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param targetId 用于定位target的标识。
     * @param masteredIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Long> findMissingPrerequisites(TenantId tenantId, Long targetId, Set<Long> masteredIds);

    /**
     * 根据条件查询并返回所需数据。
     */
    void loadAll();
}

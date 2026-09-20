package com.shiyu.ai.knowledge.implementation.application.service.graph;

import com.shiyu.ai.knowledge.implementation.application.port.graph.GraphStore;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.GraphEdge;
import com.shiyu.ai.knowledge.implementation.domain.GraphNode;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 表示 知识 相关流程的节点、边和执行关系。
 */
@Component
public class KnowledgeGraph {
    /**
     * 图结构存储，表示当前对象中的对应属性。
     */
    private final GraphStore graphStore;

    /**
     * 执行 知识 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param graphStore 用于完成本次业务处理的 graphStore 参数。
     */
    public KnowledgeGraph(GraphStore graphStore) {
        this.graphStore = graphStore;
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回 知识 相关操作生成的结果数据。
     */
    public GraphNode getNode(TenantId tenantId, Long id) {
        return graphStore.getNode(tenantId, id);
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> parents(TenantId tenantId, Long id) {
        return graphStore.parents(tenantId, id);
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> children(TenantId tenantId, Long id) {
        return graphStore.children(tenantId, id);
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> related(TenantId tenantId, Long id) {
        return graphStore.related(tenantId, id);
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<GraphEdge> edges(TenantId tenantId, Long id) {
        return graphStore.edges(tenantId, id);
    }

    /**
     * 构建或转换 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> topologicalSort(TenantId tenantId, Long id) {
        return graphStore.topologicalSort(tenantId, id);
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> dfs(TenantId tenantId, Long id) {
        return graphStore.dfs(tenantId, id);
    }

    /**
     * 执行 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> bfs(TenantId tenantId, Long id) {
        return graphStore.bfs(tenantId, id);
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param from 用于完成本次业务处理的 from 参数。
     * @param to 用于完成本次业务处理的 to 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> findPath(TenantId tenantId, Long from, Long to) {
        return graphStore.findPath(tenantId, from, to);
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @param masteredIds 待处理的业务对象标识集合。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<Long> findMissingPrerequisites(TenantId tenantId, Long id, Set<Long> masteredIds) {
        return graphStore.findMissingPrerequisites(tenantId, id, masteredIds);
    }

    /**
     * 创建或保存 知识 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param node 用于完成本次业务处理的 node 参数。
     */
    public void addNode(TenantId tenantId, GraphNode node) {
        graphStore.addNode(tenantId, node);
    }

    /**
     * 创建或保存 知识 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param weight 用于完成本次业务处理的 weight 参数。
     */
    public void addEdge(
            TenantId tenantId, Long sourceId, Long targetId, String type, double weight) {
        graphStore.addEdge(tenantId, sourceId, targetId, type, weight);
    }

    /**
     * 删除或移除 知识 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sourceId 用于定位source的标识。
     * @param targetId 用于定位target的标识。
     * @param type 用于完成本次业务处理的 type 参数。
     */
    public void removeEdge(TenantId tenantId, Long sourceId, Long targetId, String type) {
        graphStore.removeEdge(tenantId, sourceId, targetId, type);
    }

    /**
     * 删除或移除 知识 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     */
    public void removeNode(TenantId tenantId, Long id) {
        graphStore.removeNode(tenantId, id);
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<GraphNode> getParentNodes(TenantId tenantId, Long id) {
        return graphStore.getParentNodes(tenantId, id);
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<GraphNode> getChildNodes(TenantId tenantId, Long id) {
        return graphStore.getChildNodes(tenantId, id);
    }

    /**
     * 查询 知识 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param id 用于定位目标业务对象的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<GraphNode> getRelatedNodes(TenantId tenantId, Long id) {
        return graphStore.getRelatedNodes(tenantId, id);
    }

    /**
     * 执行 知识 相关业务操作，并维护必要的状态和协作关系。
     */
    public void reload() {
        graphStore.loadAll();
    }
}

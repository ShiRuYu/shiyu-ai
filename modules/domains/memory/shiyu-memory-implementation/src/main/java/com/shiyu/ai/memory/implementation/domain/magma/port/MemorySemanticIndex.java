package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

/**
 * 定义 记忆 Semantic 索引 相关的协作契约和调用边界。
 */
public interface MemorySemanticIndex {
    /**
     * 执行 记忆 Semantic 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    void upsert(MemoryEvent event);

    /**
     * 查询 记忆 Semantic 索引 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MemoryPath> search(MemoryQuery query, int limit);

    /**
     * 删除或移除 记忆 Semantic 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param eventId 用于定位event的标识。
     */
    void delete(String eventId);

    /**
     * 执行 记忆 Semantic 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     */
    void rebuild(TenantId tenantId, String namespace);
}

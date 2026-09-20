package com.shiyu.ai.knowledge.implementation.infrastructure.index.port;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 定义 Full Text 索引 相关的协作契约和调用边界。
 */
public interface FullTextIndex {

    /**
     * 查询 Full Text 索引 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param spaceId 用于定位space的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param query 用于筛选目标数据的查询条件。
     * @param topK 用于完成本次业务处理的 topK 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<FullTextHit> search(TenantId tenantId, Long spaceId, Long version, String query, int topK);

    /**
     * 封装 Full Text Hit 相关的不可变数据及其字段约束。
     */
    record FullTextHit(Long chunkId, Long documentId, float score, String highlight) {}
}

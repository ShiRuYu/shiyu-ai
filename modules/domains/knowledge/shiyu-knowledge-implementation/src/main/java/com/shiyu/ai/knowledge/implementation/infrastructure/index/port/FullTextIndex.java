package com.shiyu.ai.knowledge.implementation.infrastructure.index.port;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * FullTextIndex 接口，定义知识模块的能力边界。
 */
public interface FullTextIndex {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param version 方法参数。
     * @param query 方法参数。
     * @param topK 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<FullTextHit> search(TenantId tenantId, Long spaceId, Long version, String query, int topK);

    /**
     * {@code FullTextHit} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param chunkId chunkId 属性，表示该记录组件承载的数据。
     * @param documentId documentId 属性，表示该记录组件承载的数据。
     * @param score 分数，表示该记录组件承载的数据。
     * @param highlight highlight 属性，表示该记录组件承载的数据。
     */
    record FullTextHit(Long chunkId, Long documentId, float score, String highlight) {}
}

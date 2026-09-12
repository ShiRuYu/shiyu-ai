package com.shiyu.ai.knowledge.implementation.infrastructure.index.port;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * VectorIndex 接口，定义知识模块的能力边界。
 */
public interface VectorIndex {

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param spaceId 方法参数。
     * @param version 方法参数。
     * @param queryVector 方法参数。
     * @param topK 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<VectorHit> search(
            TenantId tenantId, Long spaceId, Long version, float[] queryVector, int topK);

    /**
     * {@code VectorHit} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param chunkId chunkId 属性，表示该记录组件承载的数据。
     * @param score 分数，表示该记录组件承载的数据。
     */
    record VectorHit(Long chunkId, double score) {}
}

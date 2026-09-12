package com.shiyu.ai.model.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * EmbeddingService 服务接口，负责执行模型领域相关业务操作。
 */
public interface EmbeddingService {
    /**
     * 生成文本嵌入向量。
     *
     * @param tenantId 租户标识。
     * @param text text 参数。
     *
     * @return 处理结果。
     */
    float[] embed(TenantId tenantId, String text);

    /**
     * 执行 {@code embed} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param text 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    default float[] embed(ActorContext actor, String text) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return embed(actor.tenantId(), text);
    }

    /**
     * 批量生成文本嵌入向量。
     *
     * @param tenantId 租户标识。
     * @param texts texts 参数。
     *
     * @return 结果列表。
     */
    List<float[]> embedBatch(TenantId tenantId, List<String> texts);

    /**
     * 处理dimension。
     *
     * @return 受影响的记录数或生成的序号。
     */
    int dimension();
}

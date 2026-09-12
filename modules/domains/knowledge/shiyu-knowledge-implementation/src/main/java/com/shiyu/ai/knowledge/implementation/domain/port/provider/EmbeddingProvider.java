package com.shiyu.ai.knowledge.implementation.domain.port.provider;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * EmbeddingProvider 边界接口，负责向外部组件提供知识领域相关能力。
 */
public interface EmbeddingProvider {
    /**
     * 执行 {@code profile} 定义的接口操作。
     *
     * @return 操作结果。
     */
    String profile();

    /**
     * 执行 {@code embed} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param text 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    float[] embed(TenantId tenantId, String text);

    /**
     * 生成文本嵌入向量。
     *
     * @param actor 调用方上下文。
     * @param text text 参数。
     *
     * @return 处理结果。
     */
    default float[] embed(ActorContext actor, String text) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return embed(actor.tenantId(), text);
    }
}

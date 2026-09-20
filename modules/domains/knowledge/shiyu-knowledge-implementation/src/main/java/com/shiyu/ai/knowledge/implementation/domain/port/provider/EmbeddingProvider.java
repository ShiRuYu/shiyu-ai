package com.shiyu.ai.knowledge.implementation.domain.port.provider;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * 创建或提供 嵌入 相关的业务组件和运行时能力。
 */
public interface EmbeddingProvider {
    /**
     * 执行 嵌入 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 相关操作生成的结果数据。
     */
    String profile();

    /**
     * 执行 嵌入 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 嵌入 相关操作生成的结果数据。
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

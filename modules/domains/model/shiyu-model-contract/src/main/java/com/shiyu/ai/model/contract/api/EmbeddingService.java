package com.shiyu.ai.model.contract.api;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 提供 嵌入 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 执行 嵌入 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 嵌入 相关操作生成的结果数据。
     */
    default float[] embed(ActorContext actor, String text) {
        if (actor == null) {
            throw new IllegalArgumentException("actor is required");
        }
        return embed(actor.tenantId(), text);
    }

    /**
     * 执行 嵌入 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param texts 用于完成本次业务处理的 texts 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<float[]> embedBatch(TenantId tenantId, List<String> texts);

    /**
     * 处理dimension。
     *
     * @return 受影响的记录数或生成的序号。
     */
    int dimension();
}

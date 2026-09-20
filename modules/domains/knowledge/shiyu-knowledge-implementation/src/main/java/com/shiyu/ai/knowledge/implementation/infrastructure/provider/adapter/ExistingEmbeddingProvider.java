package com.shiyu.ai.knowledge.implementation.infrastructure.provider.adapter;

import com.shiyu.ai.knowledge.implementation.domain.port.provider.EmbeddingProvider;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.contract.api.EmbeddingService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 创建或提供 Existing 嵌入 相关的业务组件和运行时能力。
 */
@Primary
@Component
@RequiredArgsConstructor
public class ExistingEmbeddingProvider implements EmbeddingProvider {

    /**
     * 嵌入向量服务，表示当前对象中的对应属性。
     */
    private final EmbeddingService embeddingService;

    /**
     * 执行 Existing 嵌入 相关业务数据，并返回处理结果。
     *
     * @return 返回 Existing 嵌入 相关操作生成的结果数据。
     */
    @Override
    public String profile() {
        return "default";
    }

    /**
     * 执行 Existing 嵌入 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 Existing 嵌入 相关操作生成的结果数据。
     */
    @Override
    public float[] embed(TenantId tenantId, String text) {
        return embeddingService.embed(tenantId, text);
    }

    /**
     * 执行 Existing 嵌入 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param text 用于完成本次业务处理的 text 参数。
     * @return 返回 Existing 嵌入 相关操作生成的结果数据。
     */
    @Override
    public float[] embed(ActorContext actor, String text) {
        return embeddingService.embed(actor, text);
    }
}

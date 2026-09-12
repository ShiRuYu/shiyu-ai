package com.shiyu.ai.knowledge.implementation.infrastructure.provider.adapter;

import com.shiyu.ai.knowledge.implementation.domain.port.provider.EmbeddingProvider;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.model.contract.api.EmbeddingService;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * {@code ExistingEmbeddingProvider} 承载知识模块的领域状态或协作行为，负责维护本类型的职责边界。
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
     * {@code profile} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String profile() {
        return "default";
    }

    /**
     * {@code embed} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param text 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public float[] embed(TenantId tenantId, String text) {
        return embeddingService.embed(tenantId, text);
    }

    /**
     * {@code embed} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param text 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public float[] embed(ActorContext actor, String text) {
        return embeddingService.embed(actor, text);
    }
}

package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

import java.util.Map;

/**
 * MemoryRetrievalPolicyProvider 边界接口，负责向外部组件提供记忆领域相关能力。
 */
public interface MemoryRetrievalPolicyProvider {
    /**
     * 执行 {@code weights} 定义的接口操作。
     *
     * @param query 方法参数。
     *
     * @return 操作结果。
     */
    Map<GraphType, Double> weights(MemoryQuery query);
}

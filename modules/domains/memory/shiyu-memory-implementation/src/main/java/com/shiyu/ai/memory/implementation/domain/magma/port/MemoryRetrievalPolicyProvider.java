package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

import java.util.Map;

/**
 * 创建或提供 记忆 Retrieval Policy 相关的业务组件和运行时能力。
 */
public interface MemoryRetrievalPolicyProvider {
    /**
     * 执行 记忆 Retrieval Policy 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回 记忆 Retrieval Policy 相关操作生成的结果数据。
     */
    Map<GraphType, Double> weights(MemoryQuery query);
}

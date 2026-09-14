package com.shiyu.ai.agent.contract.runtime;

import java.util.List;

/**
 * ContextRetrievalPort 边界接口，负责向外部组件提供智能体领域相关能力。
 */
public interface ContextRetrievalPort {
    /**
     * 获取上下文retrieval。
     *
     * @param query query 参数。
     *
     * @return 结果列表。
     */
    List<ContextItem> retrieve(ContextQuery query);
}

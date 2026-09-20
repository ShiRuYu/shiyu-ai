package com.shiyu.ai.agent.contract.runtime;

import java.util.List;

/**
 * 定义 Context Retrieval 领域与外部能力交互的端口契约。
 */
public interface ContextRetrievalPort {
    /**
     * 执行 Context Retrieval 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ContextItem> retrieve(ContextQuery query);
}

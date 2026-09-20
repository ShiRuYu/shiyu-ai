package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.MemoryPath;
import com.shiyu.ai.memory.contract.model.MemoryQuery;

import java.util.List;

/**
 * 定义 记忆 Query 领域与外部能力交互的端口契约。
 */
public interface MemoryQueryPort {
    /**
     * 执行 记忆 Query 相关业务数据，并返回处理结果。
     *
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MemoryPath> retrieve(MemoryQuery query);
}

package com.shiyu.ai.memory.contract.api;

import com.shiyu.ai.memory.contract.model.MemoryPath;
import com.shiyu.ai.memory.contract.model.MemoryQuery;

import java.util.List;

/**
 * MemoryQueryPort 边界接口，负责向外部组件提供记忆领域相关能力。
 */
public interface MemoryQueryPort {
    /**
     * 获取记忆query。
     *
     * @param query query 参数。
     *
     * @return 结果列表。
     */
    List<MemoryPath> retrieve(MemoryQuery query);
}

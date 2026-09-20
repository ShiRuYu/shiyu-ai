package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

/**
 * 向 记忆 关系 所属的应用或基础设施注册必要的扩展能力。
 */
public interface MemoryRelationContributor<T> {
    /**
     * 执行 记忆 关系 相关业务数据，并返回处理结果。
     *
     * @param source 用于完成本次业务处理的 source 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MemoryEdge> contribute(T source);
}

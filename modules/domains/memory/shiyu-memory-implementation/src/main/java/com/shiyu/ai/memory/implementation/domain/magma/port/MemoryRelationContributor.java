package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

/**
 * MemoryRelationContributor 接口，定义记忆模块的能力边界。
 */
public interface MemoryRelationContributor<T> {
    /**
     * 执行 {@code contribute} 定义的接口操作。
     *
     * @param source 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MemoryEdge> contribute(T source);
}

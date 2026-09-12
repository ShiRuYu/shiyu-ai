package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

/**
 * MemoryEventContributor 接口，定义记忆模块的能力边界。
 */
public interface MemoryEventContributor<T> {
    /**
     * 执行 {@code contribute} 定义的接口操作。
     *
     * @param source 方法参数。
     *
     * @return 操作结果。
     */
    IngestMemoryCommand contribute(T source);
}

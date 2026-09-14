package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

/**
 * MemoryIndexPort 边界接口，负责向外部组件提供记忆领域相关能力。
 */
public interface MemoryIndexPort {
    /**
     * 执行 {@code rebuild} 定义的接口操作。
     *
     * @param namespace 方法参数。
     */
    void rebuild(String namespace);
}

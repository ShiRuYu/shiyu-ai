package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

/**
 * 定义 记忆 索引 领域与外部能力交互的端口契约。
 */
public interface MemoryIndexPort {
    /**
     * 执行 记忆 索引 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     */
    void rebuild(String namespace);
}

package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.memory.contract.model.*;

/**
 * 向 记忆 事件 所属的应用或基础设施注册必要的扩展能力。
 */
public interface MemoryEventContributor<T> {
    /**
     * 执行 记忆 事件 相关业务数据，并返回处理结果。
     *
     * @param source 用于完成本次业务处理的 source 参数。
     * @return 返回 记忆 事件 相关操作生成的结果数据。
     */
    IngestMemoryCommand contribute(T source);
}

package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

/**
 * MemoryConsolidationPort 边界接口，负责向外部组件提供记忆领域相关能力。
 */
public interface MemoryConsolidationPort {
    /**
     * 执行 {@code retry} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param jobId 方法参数。
     */
    void retry(TenantId tenantId, long jobId);
}

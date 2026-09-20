package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

/**
 * 定义 记忆 Consolidation 领域与外部能力交互的端口契约。
 */
public interface MemoryConsolidationPort {
    /**
     * 执行 记忆 Consolidation 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param jobId 用于定位job的标识。
     */
    void retry(TenantId tenantId, long jobId);
}

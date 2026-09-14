package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

/**
 * MemoryGovernancePort 边界接口，负责向外部组件提供记忆领域相关能力。
 */
public interface MemoryGovernancePort {
    /**
     * 执行 {@code confirm} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param eventId 方法参数。
     */
    void confirm(TenantId tenantId, String eventId);

    /**
     * 执行 {@code revoke} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param eventId 方法参数。
     */
    void revoke(TenantId tenantId, String eventId);

    /**
     * 执行 {@code supersede} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param oldEventId 方法参数。
     * @param replacement 方法参数。
     *
     * @return 操作结果。
     */
    MemoryEvent supersede(TenantId tenantId, String oldEventId, IngestMemoryCommand replacement);
}

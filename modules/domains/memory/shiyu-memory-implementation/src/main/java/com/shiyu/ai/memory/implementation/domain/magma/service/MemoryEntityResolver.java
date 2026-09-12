package com.shiyu.ai.memory.implementation.domain.magma.service;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryEntity;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.util.Optional;

/**
 * MemoryEntityResolver 接口，定义记忆模块的能力边界。
 */
public interface MemoryEntityResolver {
    /**
     * 执行 {@code resolve} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param entityType 方法参数。
     * @param externalRef 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<MemoryEntity> resolve(TenantId tenantId, String entityType, String externalRef);
}

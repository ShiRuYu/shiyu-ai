package com.shiyu.ai.memory.implementation.domain.magma.port;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryEntity;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.util.Optional;

/**
 * 根据请求上下文解析或路由 记忆 Entity 相关的处理能力。
 */
public interface MemoryEntityResolver {
    /**
     * 解析或路由 记忆 Entity 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param entityType 用于完成本次业务处理的 entityType 参数。
     * @param externalRef 用于完成本次业务处理的 externalRef 参数。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<MemoryEntity> resolve(TenantId tenantId, String entityType, String externalRef);
}

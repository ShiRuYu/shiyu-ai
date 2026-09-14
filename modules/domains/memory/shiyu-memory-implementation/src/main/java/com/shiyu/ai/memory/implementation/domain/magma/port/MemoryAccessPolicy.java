package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

/**
 * MemoryAccessPolicy 接口，定义记忆模块的能力边界。
 */
public interface MemoryAccessPolicy {
    /**
     * 判断当前条件是否满足。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     * @param subjectType 方法参数。
     * @param subjectId 方法参数。
     * @param sourceType 方法参数。
     * @param sourceId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean canRead(
            TenantId tenantId,
            String namespace,
            String subjectType,
            String subjectId,
            String sourceType,
            String sourceId);
}

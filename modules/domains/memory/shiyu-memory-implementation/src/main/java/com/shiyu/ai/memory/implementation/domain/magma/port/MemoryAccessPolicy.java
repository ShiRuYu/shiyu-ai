package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

/**
 * 校验或约束 记忆 Access 相关的请求、状态和访问规则。
 */
public interface MemoryAccessPolicy {
    /**
     * 校验或判断 记忆 Access 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param subjectType 用于完成本次业务处理的 subjectType 参数。
     * @param subjectId 用于定位subject的标识。
     * @param sourceType 用于完成本次业务处理的 sourceType 参数。
     * @param sourceId 用于定位source的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean canRead(
            TenantId tenantId,
            String namespace,
            String subjectType,
            String subjectId,
            String sourceType,
            String sourceId);
}

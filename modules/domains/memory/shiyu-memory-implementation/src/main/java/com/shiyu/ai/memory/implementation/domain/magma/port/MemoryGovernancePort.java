package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

/**
 * 定义 记忆 治理 领域与外部能力交互的端口契约。
 */
public interface MemoryGovernancePort {
    /**
     * 执行 记忆 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param eventId 用于定位event的标识。
     */
    void confirm(TenantId tenantId, String eventId);

    /**
     * 执行 记忆 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param eventId 用于定位event的标识。
     */
    void revoke(TenantId tenantId, String eventId);

    /**
     * 执行 记忆 治理 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param oldEventId 用于定位old的标识。
     * @param replacement 用于完成本次业务处理的 replacement 参数。
     * @return 返回 记忆 治理 相关操作生成的结果数据。
     */
    MemoryEvent supersede(TenantId tenantId, String oldEventId, IngestMemoryCommand replacement);
}

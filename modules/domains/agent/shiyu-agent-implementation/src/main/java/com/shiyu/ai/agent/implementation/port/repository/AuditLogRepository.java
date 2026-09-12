package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * AuditLogRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface AuditLogRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param auditLog 方法参数。
     */
    void insert(TenantId tenantId, AuditLogBO auditLog);
}

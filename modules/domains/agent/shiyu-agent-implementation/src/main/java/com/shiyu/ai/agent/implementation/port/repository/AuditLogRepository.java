package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * 负责 Audit Log 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AuditLogRepository {
    /**
     * 创建或保存 Audit Log 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param auditLog 用于完成本次业务处理的 auditLog 参数。
     */
    void insert(TenantId tenantId, AuditLogBO auditLog);
}

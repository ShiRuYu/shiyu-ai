package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AuditLogBO;
import com.shiyu.ai.kernel.context.TenantId;

public interface AuditLogRepository {
    void insert(TenantId tenantId, AuditLogBO auditLog);
}

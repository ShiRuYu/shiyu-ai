package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AuditLogBO;

public interface AuditLogRepository {
    void insert(AuditLogBO auditLog);
}

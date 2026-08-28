package com.shiyu.ai.agent.persistence.repository;

import com.shiyu.ai.agent.domain.model.AuditLogBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.agent.persistence.dataobject.AuditLogDO;
import com.shiyu.ai.agent.persistence.mapper.AuditLogMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AuditLogRepositoryImpl implements com.shiyu.ai.agent.port.repository.AuditLogRepository {

    @Resource
    private AuditLogMapper auditLogMapper;

    public void insert(TenantId tenantId, AuditLogBO auditLog) {
        if (tenantId == null || tenantId.value() <= 0 || auditLog == null) {
            throw new IllegalArgumentException("audit tenantId and record are required");
        }
        if (auditLog.getUserId() == null || auditLog.getUserId() <= 0) {
            throw new IllegalArgumentException("audit userId must be positive");
        }
        auditLog.setTenantId(tenantId.value());
        auditLogMapper.insertSelective(MapstructUtils.convert(auditLog, AuditLogDO.class));
    }
}

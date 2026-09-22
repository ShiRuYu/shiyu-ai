package com.shiyu.ai.agent.implementation.persistence.repository;

import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.agent.implementation.persistence.dataobject.AuditLogDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.AuditLogMapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * 负责 Audit Log 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class AuditLogRepositoryImpl
        implements com.shiyu.ai.agent.implementation.port.repository.AuditLogRepository {

    /**
     * auditLogMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private AuditLogMapper auditLogMapper;

    public void insert(TenantId tenantId, AuditLogBO auditLog) {
        TenantScope.requireMatches(tenantId);
        if (auditLog == null) {
            throw new IllegalArgumentException("audit tenantId and record are required");
        }
        if (auditLog.getUserId() == null || auditLog.getUserId() <= 0) {
            throw new IllegalArgumentException("audit userId must be positive");
        }
        auditLog.setTenantId(tenantId.value());
        auditLogMapper.insertSelective(MapstructUtils.convert(auditLog, AuditLogDO.class));
    }
}

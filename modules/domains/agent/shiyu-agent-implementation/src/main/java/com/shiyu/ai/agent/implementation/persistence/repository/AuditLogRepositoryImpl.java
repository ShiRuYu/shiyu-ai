package com.shiyu.ai.agent.implementation.persistence.repository;

import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.agent.implementation.persistence.dataobject.AuditLogDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.AuditLogMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * {@code AuditLogRepositoryImpl} 实现智能体模块的持久化端口，负责在领域对象与存储模型之间转换。
 */
@Component
public class AuditLogRepositoryImpl
        implements com.shiyu.ai.agent.implementation.port.repository.AuditLogRepository {

    /**
     * auditLogMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private AuditLogMapper auditLogMapper;

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

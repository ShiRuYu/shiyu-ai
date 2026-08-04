package com.shiyu.ai.dal.agent.repository;

import com.shiyu.ai.agent.domain.model.AuditLogBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.agent.dataobject.AuditLogDO;
import com.shiyu.ai.dal.agent.mapper.AuditLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AuditLogRepository implements com.shiyu.ai.agent.port.repository.AuditLogRepository {

    @Resource
    private AuditLogMapper auditLogMapper;

    public void insert(AuditLogBO auditLog) {
        auditLogMapper.insertSelective(MapstructUtils.convert(auditLog, AuditLogDO.class));
    }
}

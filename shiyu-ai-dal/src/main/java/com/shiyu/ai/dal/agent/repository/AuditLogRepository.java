package com.shiyu.ai.dal.agent.repository;

import com.shiyu.ai.dal.agent.dataobject.AuditLogDO;
import com.shiyu.ai.dal.agent.mapper.AuditLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AuditLogRepository {

    @Resource
    private AuditLogMapper auditLogMapper;

    public void insert(AuditLogDO auditLogDO) {
        auditLogMapper.insertSelective(auditLogDO);
    }
}

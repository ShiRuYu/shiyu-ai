package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.persistence.dataobject.AgentDefDO;
import com.shiyu.ai.agent.persistence.mapper.AgentDefMapper;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AgentDefRepositoryImpl implements com.shiyu.ai.agent.port.repository.AgentDefRepository {

    @Resource
    private AgentDefMapper agentDefMapper;

    public long countByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        return agentDefMapper.selectCountByQuery(
            new QueryWrapper().eq("tenant_id", tenantId.value()).eq("del_flag", 0));
    }
}

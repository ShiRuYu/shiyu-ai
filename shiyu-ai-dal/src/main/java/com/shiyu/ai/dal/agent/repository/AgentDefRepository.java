package com.shiyu.ai.dal.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.agent.dataobject.AgentDefDO;
import com.shiyu.ai.dal.agent.mapper.AgentDefMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AgentDefRepository {

    @Resource
    private AgentDefMapper agentDefMapper;

    public long countByTenantId(Long tenantId) {
        return agentDefMapper.selectCountByQuery(
            new QueryWrapper().eq("tenant_id", tenantId).eq("del_flag", 0));
    }
}

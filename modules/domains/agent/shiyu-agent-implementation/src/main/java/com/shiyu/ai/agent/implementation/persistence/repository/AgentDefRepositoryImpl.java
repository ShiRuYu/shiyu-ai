package com.shiyu.ai.agent.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.implementation.persistence.mapper.AgentDefMapper;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

/**
 * 负责 智能体 Def 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class AgentDefRepositoryImpl
        implements com.shiyu.ai.agent.implementation.port.repository.AgentDefRepository {

    /**
     * agentDefMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private AgentDefMapper agentDefMapper;

    public long countByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        return agentDefMapper.selectCountByQuery(
                new QueryWrapper().eq("tenant_id", tenantId.value()).eq("del_flag", 0));
    }
}

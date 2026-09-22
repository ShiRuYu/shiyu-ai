package com.shiyu.ai.agent.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.persistence.dataobject.AgentExecutionDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.AgentExecutionMapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责 智能体 Execution 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class AgentExecutionRepositoryImpl
        implements com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository {

    /**
     * agentExecutionMapper 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Resource private AgentExecutionMapper agentExecutionMapper;

    @Override
    public void insert(TenantId tenantId, AgentExecutionBO bo) {
        AgentExecutionDO execution = MapstructUtils.convert(bo, AgentExecutionDO.class);
        execution.setTenantId(tenantId.value());
        agentExecutionMapper.insertSelective(execution);
        bo.setId(execution.getId());
    }

    @Override
    public void update(TenantId tenantId, AgentExecutionBO bo) {
        AgentExecutionDO execution = MapstructUtils.convert(bo, AgentExecutionDO.class);
        execution.setTenantId(tenantId.value());
        agentExecutionMapper.update(execution);
    }

    @Override
    public AgentExecutionBO selectByExecutionId(TenantId tenantId, String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getTenantId, tenantId.value());
        qw.eq(AgentExecutionDO::getExecutionId, executionId);
        AgentExecutionDO d = agentExecutionMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(d, AgentExecutionBO.class);
    }

    @Override
    public List<AgentExecutionBO> selectBySessionId(TenantId tenantId, String sessionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getTenantId, tenantId.value());
        qw.eq(AgentExecutionDO::getSessionId, sessionId);
        qw.orderBy(AgentExecutionDO::getStartTime, true);
        List<AgentExecutionDO> doList = agentExecutionMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, AgentExecutionBO.class);
    }

    @Override
    public List<AgentExecutionBO> selectByAgentId(TenantId tenantId, String agentId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getTenantId, tenantId.value());
        qw.eq(AgentExecutionDO::getAgentId, agentId);
        qw.orderBy(AgentExecutionDO::getStartTime, false);
        qw.limit(Math.max(1, Math.min(limit, 100)));
        List<AgentExecutionDO> doList = agentExecutionMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, AgentExecutionBO.class);
    }
}

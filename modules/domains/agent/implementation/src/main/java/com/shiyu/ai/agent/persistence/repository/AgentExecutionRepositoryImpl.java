package com.shiyu.ai.agent.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.persistence.dataobject.AgentExecutionDO;
import com.shiyu.ai.agent.persistence.mapper.AgentExecutionMapper;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentExecutionRepositoryImpl implements com.shiyu.ai.agent.port.repository.AgentExecutionRepository {

    @Resource
    private AgentExecutionMapper agentExecutionMapper;

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

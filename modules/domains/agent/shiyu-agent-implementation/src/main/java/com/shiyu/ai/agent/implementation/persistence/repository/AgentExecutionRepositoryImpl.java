package com.shiyu.ai.agent.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.implementation.domain.enums.AgentExecutionStatus;
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
    public int updateIfStatus(TenantId tenantId, AgentExecutionBO bo, Integer expectedStatus) {
        if (tenantId == null || bo == null || bo.getExecutionId() == null
                || bo.getExecutionId().isBlank() || expectedStatus == null) {
            throw new IllegalArgumentException("execution tenant, ID, and expected status are required");
        }
        AgentExecutionDO update = MapstructUtils.convert(bo, AgentExecutionDO.class);
        update.setTenantId(tenantId.value());
        QueryWrapper query = new QueryWrapper();
        query.eq(AgentExecutionDO::getTenantId, tenantId.value());
        query.eq(AgentExecutionDO::getExecutionId, bo.getExecutionId());
        query.eq(AgentExecutionDO::getStatus, expectedStatus);
        query.in(
                AgentExecutionDO::getStatus,
                AgentExecutionStatus.RUNNING.getCode(),
                AgentExecutionStatus.PAUSED.getCode());
        return agentExecutionMapper.updateByQuery(update, query);
    }

    @Override
    public int updateCheckpointIfStatus(
            TenantId tenantId, String executionId, String checkpointId, Integer expectedStatus) {
        if (tenantId == null || executionId == null || executionId.isBlank()
                || expectedStatus == null) {
            throw new IllegalArgumentException("execution tenant, ID, and expected status are required");
        }
        AgentExecutionDO update = new AgentExecutionDO();
        update.setTenantId(tenantId.value());
        update.setLastCheckpointId(checkpointId);
        QueryWrapper query = new QueryWrapper();
        query.eq(AgentExecutionDO::getTenantId, tenantId.value());
        query.eq(AgentExecutionDO::getExecutionId, executionId);
        query.eq(AgentExecutionDO::getStatus, expectedStatus);
        return agentExecutionMapper.updateByQuery(update, query);
    }

    @Override
    public boolean claimPausedForResume(
            TenantId tenantId, String executionId, String expectedCheckpointId) {
        if (tenantId == null || executionId == null || executionId.isBlank()) {
            throw new IllegalArgumentException("execution tenant and ID are required");
        }
        QueryWrapper query = new QueryWrapper();
        query.eq(AgentExecutionDO::getTenantId, tenantId.value());
        query.eq(AgentExecutionDO::getExecutionId, executionId);
        query.eq(AgentExecutionDO::getStatus, AgentExecutionStatus.PAUSED.getCode());
        if (expectedCheckpointId == null) {
            query.isNull(AgentExecutionDO::getLastCheckpointId);
        } else {
            query.eq(AgentExecutionDO::getLastCheckpointId, expectedCheckpointId);
        }
        AgentExecutionDO update = new AgentExecutionDO();
        update.setStatus(AgentExecutionStatus.RUNNING.getCode());
        return agentExecutionMapper.updateByQuery(update, query) == 1;
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

    @Override
    public List<AgentExecutionBO> selectByUserId(TenantId tenantId, long userId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getTenantId, tenantId.value());
        qw.eq(AgentExecutionDO::getUserId, userId);
        qw.orderBy(AgentExecutionDO::getStartTime, false);
        qw.limit(Math.max(1, Math.min(limit, 100)));
        return MapstructUtils.convert(
                agentExecutionMapper.selectListByQuery(qw), AgentExecutionBO.class);
    }

    @Override
    public List<AgentExecutionBO> selectByAgentIdAndUserId(
            TenantId tenantId, String agentId, long userId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getTenantId, tenantId.value());
        qw.eq(AgentExecutionDO::getAgentId, agentId);
        qw.eq(AgentExecutionDO::getUserId, userId);
        qw.orderBy(AgentExecutionDO::getStartTime, false);
        qw.limit(Math.max(1, Math.min(limit, 100)));
        return MapstructUtils.convert(
                agentExecutionMapper.selectListByQuery(qw), AgentExecutionBO.class);
    }
}

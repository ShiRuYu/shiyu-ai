package com.shiyu.ai.aiagent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.agent.AgentExecutionDO;
import com.shiyu.ai.dal.mapper.agent.AgentExecutionMapper;
import com.shiyu.ai.aiagent.bo.AgentExecutionBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentExecutionRepository {

    @Resource
    private AgentExecutionMapper agentExecutionMapper;

    public void insert(AgentExecutionBO bo) {
        AgentExecutionDO execution = MapstructUtils.convert(bo, AgentExecutionDO.class);
        agentExecutionMapper.insertSelective(execution);
        bo.setId(execution.getId());
    }

    public void update(AgentExecutionBO bo) {
        AgentExecutionDO execution = MapstructUtils.convert(bo, AgentExecutionDO.class);
        agentExecutionMapper.update(execution);
    }

    public AgentExecutionBO selectByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getExecutionId, executionId);
        AgentExecutionDO d = agentExecutionMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(d, AgentExecutionBO.class);
    }

    public List<AgentExecutionBO> selectBySessionId(String sessionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getSessionId, sessionId);
        qw.orderBy(AgentExecutionDO::getStartTime, true);
        List<AgentExecutionDO> doList = agentExecutionMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, AgentExecutionBO.class);
    }

    public List<AgentExecutionBO> selectByAgentId(String agentId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getAgentId, agentId);
        qw.orderBy(AgentExecutionDO::getStartTime, false);
        qw.limit(limit);
        List<AgentExecutionDO> doList = agentExecutionMapper.selectListByQuery(qw);
        return MapstructUtils.convert(doList, AgentExecutionBO.class);
    }
}

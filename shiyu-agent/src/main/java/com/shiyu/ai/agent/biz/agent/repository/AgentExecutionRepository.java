package com.shiyu.ai.agent.biz.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.dal.dataobject.agent.AgentExecutionDO;
import com.shiyu.ai.agent.dal.mapper.agent.AgentExecutionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentExecutionRepository {

    @Resource
    private AgentExecutionMapper agentExecutionMapper;

    public void insert(AgentExecutionDO execution) {
        agentExecutionMapper.insertSelective(execution);
    }

    public void update(AgentExecutionDO execution) {
        agentExecutionMapper.update(execution);
    }

    public AgentExecutionDO selectByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getExecutionId, executionId);
        return agentExecutionMapper.selectOneByQuery(qw);
    }

    public List<AgentExecutionDO> selectBySessionId(String sessionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getSessionId, sessionId);
        qw.orderBy(AgentExecutionDO::getStartTime, true);
        return agentExecutionMapper.selectListByQuery(qw);
    }

    public List<AgentExecutionDO> selectByAgentId(String agentId, int limit) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentExecutionDO::getAgentId, agentId);
        qw.orderBy(AgentExecutionDO::getStartTime, false);
        qw.limit(limit);
        return agentExecutionMapper.selectListByQuery(qw);
    }
}

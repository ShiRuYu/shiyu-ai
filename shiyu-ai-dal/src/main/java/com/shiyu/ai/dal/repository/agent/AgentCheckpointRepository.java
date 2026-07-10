package com.shiyu.ai.dal.repository.agent;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.dataobject.agent.AgentCheckpointDO;
import com.shiyu.ai.dal.mapper.agent.AgentCheckpointMapper;
import com.shiyu.ai.dal.bo.agent.AgentCheckpointBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentCheckpointRepository {

    @Resource
    private AgentCheckpointMapper agentCheckpointMapper;

    public void insert(AgentCheckpointBO bo) {
        AgentCheckpointDO d = MapstructUtils.convert(bo, AgentCheckpointDO.class);
        agentCheckpointMapper.insertSelective(d);
        bo.setId(d.getId());
    }

    public AgentCheckpointBO selectByCheckpointId(String checkpointId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentCheckpointDO::getCheckpointId, checkpointId);
        AgentCheckpointDO d = agentCheckpointMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(d, AgentCheckpointBO.class);
    }

    public AgentCheckpointBO selectLatestByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentCheckpointDO::getExecutionId, executionId);
        qw.orderBy(AgentCheckpointDO::getCreateTime, false);
        AgentCheckpointDO d = agentCheckpointMapper.selectOneByQuery(qw);
        return MapstructUtils.convert(d, AgentCheckpointBO.class);
    }

    public void deleteByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq(AgentCheckpointDO::getExecutionId, executionId);
        agentCheckpointMapper.deleteByQuery(qw);
    }
}

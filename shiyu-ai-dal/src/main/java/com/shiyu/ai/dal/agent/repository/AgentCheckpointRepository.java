package com.shiyu.ai.dal.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.agent.dataobject.AgentCheckpointDO;
import com.shiyu.ai.dal.agent.mapper.AgentCheckpointMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentCheckpointRepository {

    @Resource
    private AgentCheckpointMapper agentCheckpointMapper;

    public void insert(AgentCheckpointDO checkpoint) {
        agentCheckpointMapper.insertSelective(checkpoint);
    }

    public AgentCheckpointDO selectByCheckpointId(String checkpointId) {
        return agentCheckpointMapper.selectOneByQuery(
            new QueryWrapper().eq("checkpoint_id", checkpointId));
    }

    public AgentCheckpointDO selectLatestByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("execution_id", executionId);
        qw.orderBy("create_time", false);
        qw.limit(1);
        return agentCheckpointMapper.selectOneByQuery(qw);
    }

    public void deleteByCheckpointId(String checkpointId) {
        agentCheckpointMapper.deleteByQuery(
            new QueryWrapper().eq("checkpoint_id", checkpointId));
    }

    public void deleteByExecutionId(String executionId) {
        agentCheckpointMapper.deleteByQuery(
            new QueryWrapper().eq("execution_id", executionId));
    }

    public List<AgentCheckpointDO> listByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("execution_id", executionId);
        qw.orderBy("create_time", true);
        return agentCheckpointMapper.selectListByQuery(qw);
    }
}

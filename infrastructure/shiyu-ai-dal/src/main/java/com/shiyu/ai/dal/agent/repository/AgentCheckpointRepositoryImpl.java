package com.shiyu.ai.dal.agent.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.dal.agent.dataobject.AgentCheckpointDO;
import com.shiyu.ai.dal.agent.mapper.AgentCheckpointMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentCheckpointRepositoryImpl implements com.shiyu.ai.agent.port.repository.AgentCheckpointRepository {

    @Resource
    private AgentCheckpointMapper agentCheckpointMapper;

    public void insert(AgentCheckpointBO checkpoint) {
        AgentCheckpointDO data = MapstructUtils.convert(checkpoint, AgentCheckpointDO.class);
        agentCheckpointMapper.insertSelective(data);
        checkpoint.setId(data.getId());
    }

    public AgentCheckpointBO selectByCheckpointId(String checkpointId) {
        return MapstructUtils.convert(agentCheckpointMapper.selectOneByQuery(
            new QueryWrapper().eq("checkpoint_id", checkpointId)), AgentCheckpointBO.class);
    }

    public AgentCheckpointBO selectLatestByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("execution_id", executionId);
        qw.orderBy("create_time", false);
        qw.limit(1);
        return MapstructUtils.convert(agentCheckpointMapper.selectOneByQuery(qw), AgentCheckpointBO.class);
    }

    public void deleteByCheckpointId(String checkpointId) {
        agentCheckpointMapper.deleteByQuery(
            new QueryWrapper().eq("checkpoint_id", checkpointId));
    }

    public void deleteByExecutionId(String executionId) {
        agentCheckpointMapper.deleteByQuery(
            new QueryWrapper().eq("execution_id", executionId));
    }

    public List<AgentCheckpointBO> listByExecutionId(String executionId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("execution_id", executionId);
        qw.orderBy("create_time", true);
        return MapstructUtils.convert(agentCheckpointMapper.selectListByQuery(qw), AgentCheckpointBO.class);
    }
}

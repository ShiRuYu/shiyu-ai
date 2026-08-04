package com.shiyu.ai.agent.checkpoint;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import com.shiyu.ai.agent.port.repository.AgentCheckpointRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 基于数据库的检查点存储
 */
@Slf4j
public class DbCheckpointStore implements CheckpointStore {

    private final AgentCheckpointRepository checkpointRepository;

    public DbCheckpointStore(AgentCheckpointRepository checkpointRepository) {
        this.checkpointRepository = checkpointRepository;
    }

    @Override
    public void save(Checkpoint checkpoint) {
        AgentCheckpointBO doObj = new AgentCheckpointBO();
        doObj.setCheckpointId(checkpoint.getCheckpointId());
        doObj.setExecutionId(checkpoint.getExecutionId());
        doObj.setNodeId(checkpoint.getNodeId());
        doObj.setStateData(JSONUtils.toJsonString(checkpoint.getState()));
        doObj.setCreateTime(LocalDateTime.now());
        checkpointRepository.insert(doObj);
    }

    @Override
    public Checkpoint load(String checkpointId) {
        AgentCheckpointBO doObj = checkpointRepository.selectByCheckpointId(checkpointId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    @Override
    public Checkpoint loadByExecutionId(String executionId) {
        AgentCheckpointBO doObj = checkpointRepository.selectLatestByExecutionId(executionId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    @Override
    public void delete(String checkpointId) {
        checkpointRepository.deleteByCheckpointId(checkpointId);
    }

    @Override
    public void deleteByExecutionId(String executionId) {
        checkpointRepository.deleteByExecutionId(executionId);
    }

    @Override
    public List<Checkpoint> listByExecutionId(String executionId) {
        return checkpointRepository.listByExecutionId(executionId)
                .stream().map(this::toCheckpoint).collect(Collectors.toList());
    }

    private Checkpoint toCheckpoint(AgentCheckpointBO doObj) {
        Checkpoint cp = new Checkpoint(
                doObj.getExecutionId(),
                doObj.getNodeId(),
                JSONUtils.parseObject(doObj.getStateData(), Map.class)
        );
        return cp;
    }
}

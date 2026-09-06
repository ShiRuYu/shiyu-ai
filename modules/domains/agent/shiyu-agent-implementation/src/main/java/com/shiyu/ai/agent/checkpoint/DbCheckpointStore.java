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
import com.shiyu.ai.kernel.context.TenantId;

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
    public void save(TenantId tenantId, Checkpoint checkpoint) {
        if (checkpoint == null || !tenantId.equals(checkpoint.getTenantId())) {
            throw new IllegalArgumentException("checkpoint tenant does not match request");
        }
        AgentCheckpointBO doObj = new AgentCheckpointBO();
        doObj.setCheckpointId(checkpoint.getCheckpointId());
        doObj.setExecutionId(checkpoint.getExecutionId());
        doObj.setNodeId(checkpoint.getNodeId());
        doObj.setStateData(JSONUtils.toJsonString(checkpoint.getState()));
        doObj.setCreateTime(LocalDateTime.now());
        checkpointRepository.insert(tenantId, doObj);
    }

    @Override
    public Checkpoint load(TenantId tenantId, String checkpointId) {
        AgentCheckpointBO doObj = checkpointRepository.selectByCheckpointId(tenantId, checkpointId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    @Override
    public Checkpoint loadByExecutionId(TenantId tenantId, String executionId) {
        AgentCheckpointBO doObj = checkpointRepository.selectLatestByExecutionId(tenantId, executionId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    @Override
    public void delete(TenantId tenantId, String checkpointId) {
        checkpointRepository.deleteByCheckpointId(tenantId, checkpointId);
    }

    @Override
    public void deleteByExecutionId(TenantId tenantId, String executionId) {
        checkpointRepository.deleteByExecutionId(tenantId, executionId);
    }

    @Override
    public List<Checkpoint> listByExecutionId(TenantId tenantId, String executionId) {
        return checkpointRepository.listByExecutionId(tenantId, executionId)
                .stream().map(this::toCheckpoint).collect(Collectors.toList());
    }

    private Checkpoint toCheckpoint(AgentCheckpointBO doObj) {
        Checkpoint cp = new Checkpoint(
                new TenantId(doObj.getTenantId()),
                doObj.getExecutionId(),
                doObj.getNodeId(),
                JSONUtils.parseMap(doObj.getStateData())
        );
        return cp;
    }
}

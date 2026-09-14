package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.agent.implementation.domain.model.AgentCheckpointBO;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** 基于数据库的检查点存储 */
@Slf4j
public class DbCheckpointStore implements CheckpointStore {

    /**
     * checkpointRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentCheckpointRepository checkpointRepository;

    /**
     * {@code DbCheckpointStore} 创建并初始化当前类型实例。
     *
     * @param checkpointRepository 参数值，用于执行当前操作。
     */
    public DbCheckpointStore(AgentCheckpointRepository checkpointRepository) {
        this.checkpointRepository = checkpointRepository;
    }

    /**
     * {@code save} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param checkpoint 参数值，用于执行当前操作。
     */
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

    /**
     * {@code load} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param checkpointId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Checkpoint load(TenantId tenantId, String checkpointId) {
        AgentCheckpointBO doObj = checkpointRepository.selectByCheckpointId(tenantId, checkpointId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    /**
     * {@code loadByExecutionId} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Checkpoint loadByExecutionId(TenantId tenantId, String executionId) {
        AgentCheckpointBO doObj =
                checkpointRepository.selectLatestByExecutionId(tenantId, executionId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param checkpointId 参数值，用于执行当前操作。
     */
    @Override
    public void delete(TenantId tenantId, String checkpointId) {
        checkpointRepository.deleteByCheckpointId(tenantId, checkpointId);
    }

    /**
     * {@code deleteByExecutionId} 释放或移除当前操作涉及的资源。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     */
    @Override
    public void deleteByExecutionId(TenantId tenantId, String executionId) {
        checkpointRepository.deleteByExecutionId(tenantId, executionId);
    }

    /**
     * {@code listByExecutionId} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Checkpoint> listByExecutionId(TenantId tenantId, String executionId) {
        return checkpointRepository.listByExecutionId(tenantId, executionId).stream()
                .map(this::toCheckpoint)
                .collect(Collectors.toList());
    }

    private Checkpoint toCheckpoint(AgentCheckpointBO doObj) {
        Checkpoint cp =
                new Checkpoint(
                        new TenantId(doObj.getTenantId()),
                        doObj.getExecutionId(),
                        doObj.getNodeId(),
                        JSONUtils.parseMap(doObj.getStateData()));
        return cp;
    }
}

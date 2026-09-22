package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.agent.implementation.domain.model.AgentCheckpointBO;
import com.shiyu.ai.agent.implementation.port.repository.AgentCheckpointRepository;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理 Db Checkpoint 相关的运行时状态、注册信息或临时数据。
 */
@Slf4j
public class DbCheckpointStore implements CheckpointStore {

    /**
     * checkpointRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentCheckpointRepository checkpointRepository;

    /**
     * 执行 Db Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param checkpointRepository 用于完成本次业务处理的 checkpointRepository 参数。
     */
    public DbCheckpointStore(AgentCheckpointRepository checkpointRepository) {
        this.checkpointRepository = checkpointRepository;
    }

    /**
     * 创建或保存 Db Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param checkpoint 用于完成本次业务处理的 checkpoint 参数。
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
     * 执行 Db Checkpoint 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param checkpointId 用于定位checkpoint的标识。
     * @return 返回 Db Checkpoint 相关操作生成的结果数据。
     */
    @Override
    public Checkpoint load(TenantId tenantId, String checkpointId) {
        AgentCheckpointBO doObj = checkpointRepository.selectByCheckpointId(tenantId, checkpointId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    /**
     * 执行 Db Checkpoint 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回 Db Checkpoint 相关操作生成的结果数据。
     */
    @Override
    public Checkpoint loadByExecutionId(TenantId tenantId, String executionId) {
        AgentCheckpointBO doObj =
                checkpointRepository.selectLatestByExecutionId(tenantId, executionId);
        return doObj != null ? toCheckpoint(doObj) : null;
    }

    /**
     * 删除或移除 Db Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param checkpointId 用于定位checkpoint的标识。
     */
    @Override
    public void delete(TenantId tenantId, String checkpointId) {
        checkpointRepository.deleteByCheckpointId(tenantId, checkpointId);
    }

    /**
     * 删除或移除 Db Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     */
    @Override
    public void deleteByExecutionId(TenantId tenantId, String executionId) {
        checkpointRepository.deleteByExecutionId(tenantId, executionId);
    }

    /**
     * 查询 Db Checkpoint 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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

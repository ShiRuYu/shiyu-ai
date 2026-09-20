package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.kernel.context.TenantId;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 管理 Checkpoint 相关的运行时状态、注册信息或临时数据。
 */
@Slf4j
public class CheckpointManager {

    /**
     * checkpointStore 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CheckpointStore checkpointStore;

    /**
     * 校验或判断 Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param checkpointStore 用于完成本次业务处理的 checkpointStore 参数。
     */
    public CheckpointManager(CheckpointStore checkpointStore) {
        this.checkpointStore = checkpointStore;
    }

    /** 创建检查点 */
    public Checkpoint createCheckpoint(
            TenantId tenantId, String executionId, String nodeId, Map<String, Object> state) {
        Checkpoint checkpoint = new Checkpoint(tenantId, executionId, nodeId, state);
        checkpointStore.save(tenantId, checkpoint);
        log.debug(
                "检查点已创建: executionIdPresent={}, nodeIdPresent={}, checkpointIdPresent={}",
                executionId != null,
                nodeId != null,
                checkpoint.getCheckpointId() != null);
        return checkpoint;
    }

    /** 获取最新的检查点 */
    public Checkpoint loadLatestCheckpoint(TenantId tenantId, String executionId) {
        return checkpointStore.loadByExecutionId(tenantId, executionId);
    }

    /** 按 ID 加载检查点 */
    public Checkpoint loadCheckpoint(TenantId tenantId, String checkpointId) {
        return checkpointStore.load(tenantId, checkpointId);
    }

    /** 清理执行相关的检查点 */
    public void cleanCheckpoints(TenantId tenantId, String executionId) {
        checkpointStore.deleteByExecutionId(tenantId, executionId);
        log.debug("检查点已清理: executionIdPresent={}", executionId != null);
    }
}

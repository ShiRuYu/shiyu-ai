package com.shiyu.ai.agent.checkpoint;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * 检查点管理器
 */
@Slf4j
public class CheckpointManager {

    private final CheckpointStore checkpointStore;

    public CheckpointManager(CheckpointStore checkpointStore) {
        this.checkpointStore = checkpointStore;
    }

    /**
     * 创建检查点
     */
    public Checkpoint createCheckpoint(TenantId tenantId, String executionId, String nodeId, Map<String, Object> state) {
        Checkpoint checkpoint = new Checkpoint(tenantId, executionId, nodeId, state);
        checkpointStore.save(tenantId, checkpoint);
        log.debug("检查点已创建: executionId={}, nodeId={}, checkpointId={}",
                executionId, nodeId, checkpoint.getCheckpointId());
        return checkpoint;
    }

    /**
     * 获取最新的检查点
     */
    public Checkpoint loadLatestCheckpoint(TenantId tenantId, String executionId) {
        return checkpointStore.loadByExecutionId(tenantId, executionId);
    }

    /**
     * 按 ID 加载检查点
     */
    public Checkpoint loadCheckpoint(TenantId tenantId, String checkpointId) {
        return checkpointStore.load(tenantId, checkpointId);
    }

    /**
     * 清理执行相关的检查点
     */
    public void cleanCheckpoints(TenantId tenantId, String executionId) {
        checkpointStore.deleteByExecutionId(tenantId, executionId);
        log.debug("检查点已清理: executionId={}", executionId);
    }
}

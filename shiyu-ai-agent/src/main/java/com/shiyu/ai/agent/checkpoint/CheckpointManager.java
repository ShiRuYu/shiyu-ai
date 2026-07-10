package com.shiyu.ai.agent.checkpoint;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
    public Checkpoint createCheckpoint(String executionId, String nodeId, Map<String, Object> state) {
        Checkpoint checkpoint = new Checkpoint(executionId, nodeId, state);
        checkpointStore.save(checkpoint);
        log.debug("检查点已创建: executionId={}, nodeId={}, checkpointId={}",
                executionId, nodeId, checkpoint.getCheckpointId());
        return checkpoint;
    }

    /**
     * 获取最新的检查点
     */
    public Checkpoint loadLatestCheckpoint(String executionId) {
        return checkpointStore.loadByExecutionId(executionId);
    }

    /**
     * 按 ID 加载检查点
     */
    public Checkpoint loadCheckpoint(String checkpointId) {
        return checkpointStore.load(checkpointId);
    }

    /**
     * 清理执行相关的检查点
     */
    public void cleanCheckpoints(String executionId) {
        checkpointStore.deleteByExecutionId(executionId);
        log.debug("检查点已清理: executionId={}", executionId);
    }
}

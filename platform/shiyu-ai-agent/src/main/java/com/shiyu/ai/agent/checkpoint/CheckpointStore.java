package com.shiyu.ai.agent.checkpoint;

import java.util.List;

/**
 * 检查点存储接口
 */
public interface CheckpointStore {

    void save(Checkpoint checkpoint);

    Checkpoint load(String checkpointId);

    Checkpoint loadByExecutionId(String executionId);

    void delete(String checkpointId);

    void deleteByExecutionId(String executionId);

    List<Checkpoint> listByExecutionId(String executionId);
}

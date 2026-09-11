package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/** 检查点存储接口 */
public interface CheckpointStore {

    void save(TenantId tenantId, Checkpoint checkpoint);

    Checkpoint load(TenantId tenantId, String checkpointId);

    Checkpoint loadByExecutionId(TenantId tenantId, String executionId);

    void delete(TenantId tenantId, String checkpointId);

    void deleteByExecutionId(TenantId tenantId, String executionId);

    List<Checkpoint> listByExecutionId(TenantId tenantId, String executionId);
}

package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/** 检查点存储接口 */
public interface CheckpointStore {

    /**
     * 保存或更新业务对象。
     *
     * @param tenantId 租户标识。
     * @param checkpoint 方法参数。
     */
    void save(TenantId tenantId, Checkpoint checkpoint);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param checkpointId 方法参数。
     *
     * @return 操作结果。
     */
    Checkpoint load(TenantId tenantId, String checkpointId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     *
     * @return 操作结果。
     */
    Checkpoint loadByExecutionId(TenantId tenantId, String executionId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param checkpointId 方法参数。
     */
    void delete(TenantId tenantId, String checkpointId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     */
    void deleteByExecutionId(TenantId tenantId, String executionId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Checkpoint> listByExecutionId(TenantId tenantId, String executionId);
}

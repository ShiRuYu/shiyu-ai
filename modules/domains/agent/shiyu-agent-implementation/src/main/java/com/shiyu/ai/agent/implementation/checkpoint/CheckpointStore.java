package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 管理 Checkpoint 相关的运行时状态、注册信息或临时数据。
 */
public interface CheckpointStore {

    /**
     * 创建或保存 Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param checkpoint 用于完成本次业务处理的 checkpoint 参数。
     */
    void save(TenantId tenantId, Checkpoint checkpoint);

    /**
     * 执行 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param checkpointId 用于定位checkpoint的标识。
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    Checkpoint load(TenantId tenantId, String checkpointId);

    /**
     * 执行 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    Checkpoint loadByExecutionId(TenantId tenantId, String executionId);

    /**
     * 删除或移除 Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param checkpointId 用于定位checkpoint的标识。
     */
    void delete(TenantId tenantId, String checkpointId);

    /**
     * 删除或移除 Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     */
    void deleteByExecutionId(TenantId tenantId, String executionId);

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Checkpoint> listByExecutionId(TenantId tenantId, String executionId);
}

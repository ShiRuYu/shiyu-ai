package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 实现 Checkpoint 相关的业务处理、协作逻辑或基础设施能力。
 */
public class Checkpoint {

    /**
     * checkpointId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String checkpointId;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private final TenantId tenantId;
    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private final String executionId;
    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String nodeId;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private final Map<String, Object> state;
    /**
     * serializedState 属性，保存当前对象中的业务数据或协作依赖。
     */
    private byte[] serializedState;
    /**
     * 创建时间，表示当前对象中的对应属性。
     */
    private final LocalDateTime createdAt;

    /**
     * 校验或判断 Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @param nodeId 用于定位node的标识。
     * @param state 用于完成本次业务处理的 state 参数。
     */
    public Checkpoint(
            TenantId tenantId, String executionId, String nodeId, Map<String, Object> state) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId must not be null");
        this.checkpointId = UUID.randomUUID().toString().replace("-", "");
        this.tenantId = tenantId;
        this.executionId = executionId;
        this.nodeId = nodeId;
        this.state = state;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    public String getCheckpointId() {
        return checkpointId;
    }

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    public Map<String, Object> getState() {
        return state;
    }

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    public byte[] getSerializedState() {
        return serializedState;
    }

    /**
     * 查询 Checkpoint 相关业务数据，并返回处理结果。
     *
     * @return 返回 Checkpoint 相关操作生成的结果数据。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 更新或设置 Checkpoint 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param serializedState 用于完成本次业务处理的 serializedState 参数。
     */
    public void setSerializedState(byte[] serializedState) {
        this.serializedState = serializedState;
    }
}

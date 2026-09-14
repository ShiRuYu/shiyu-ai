package com.shiyu.ai.agent.implementation.checkpoint;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/** 检查点数据 */
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
     * {@code Checkpoint} 创建并初始化当前类型实例。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     * @param nodeId 参数值，用于执行当前操作。
     * @param state 参数值，用于执行当前操作。
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
     * {@code getCheckpointId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getCheckpointId() {
        return checkpointId;
    }

    /**
     * {@code getTenantId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * {@code getExecutionId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getExecutionId() {
        return executionId;
    }

    /**
     * {@code getNodeId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * {@code getState} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> getState() {
        return state;
    }

    /**
     * {@code getSerializedState} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public byte[] getSerializedState() {
        return serializedState;
    }

    /**
     * {@code getCreatedAt} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * {@code setSerializedState} 写入或更新当前模块中的业务数据。
     *
     * @param serializedState 参数值，用于执行当前操作。
     */
    public void setSerializedState(byte[] serializedState) {
        this.serializedState = serializedState;
    }
}

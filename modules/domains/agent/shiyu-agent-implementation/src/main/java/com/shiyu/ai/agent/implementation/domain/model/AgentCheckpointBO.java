package com.shiyu.ai.agent.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表示 智能体 Checkpoint 领域对象的业务状态和属性。
 */
@Data
public class AgentCheckpointBO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;

    /**
     * checkpointId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String checkpointId;

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private String executionId;

    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeId;

    /**
     * 状态数据，表示当前对象中的对应属性。
     */
    private String stateData;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}

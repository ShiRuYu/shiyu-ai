package com.shiyu.ai.agent.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {@code ExecutionTimelineBO} 是智能体模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
public class ExecutionTimelineBO implements Serializable {

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
     * 执行标识，表示当前对象中的对应属性。
     */
    private String executionId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;
    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeId;
    /**
     * nodeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeType;
    /**
     * eventType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String eventType;
    /**
     * payload 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String payload;
    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long durationMs;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}

package com.shiyu.ai.agent.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 节点执行记录业务对象 */
@Data
public class NodeExecutionBO implements Serializable {

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
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeId;

    /**
     * nodeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeType;

    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;

    /**
     * statusDesc 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String statusDesc;

    /**
     * 输入数据，表示当前对象中的对应属性。
     */
    private String inputData;

    /**
     * 输出数据，表示当前对象中的对应属性。
     */
    private String outputData;

    /**
     * 错误消息，表示当前对象中的对应属性。
     */
    private String errorMessage;

    /**
     * startTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime startTime;

    /**
     * endTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime endTime;

    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long durationMs;

    /**
     * retryCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer retryCount;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}

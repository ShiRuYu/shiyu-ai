package com.shiyu.ai.agent.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 节点执行记录业务对象
 */
@Data
public class NodeExecutionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String executionId;

    private String nodeId;

    private String nodeType;

    private Integer status;

    private String statusDesc;

    private String inputData;

    private String outputData;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private Integer retryCount;

    private LocalDateTime createTime;
}

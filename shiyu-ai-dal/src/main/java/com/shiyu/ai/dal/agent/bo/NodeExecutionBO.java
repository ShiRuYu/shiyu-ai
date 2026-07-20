package com.shiyu.ai.dal.agent.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.agent.dataobject.NodeExecutionDO;

@Data
@AutoMapper(target = NodeExecutionDO.class, reverseConvertGenerate = true)
public class NodeExecutionBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String executionId;

    private String nodeId;

    private String nodeType;

    private String status;

    private String inputData;

    private String outputData;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private Integer retryCount;

    private LocalDateTime createTime;
}

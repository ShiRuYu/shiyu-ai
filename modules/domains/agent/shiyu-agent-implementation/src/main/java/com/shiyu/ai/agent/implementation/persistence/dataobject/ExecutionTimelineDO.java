package com.shiyu.ai.agent.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("observation_execution_timeline")
@AutoMapper(target = ExecutionTimelineBO.class, reverseConvertGenerate = true)
public class ExecutionTimelineDO implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long tenantId;
    private String executionId;
    private String agentId;
    private String nodeId;
    private String nodeType;
    private String eventType;
    private String payload;
    private Long durationMs;
    private LocalDateTime createTime;
}

package com.shiyu.ai.agent.domain.model;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ExecutionTimelineBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
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

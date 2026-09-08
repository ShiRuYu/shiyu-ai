package com.shiyu.ai.agent.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AgentCheckpointBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private String checkpointId;

    private String executionId;

    private String nodeId;

    private String stateData;

    private LocalDateTime createTime;
}

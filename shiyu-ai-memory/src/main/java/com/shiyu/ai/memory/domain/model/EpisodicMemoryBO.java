package com.shiyu.ai.memory.domain.model;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 情景记忆业务对象
 */
@Data
public class EpisodicMemoryBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String executionId;
    private String agentId;
    private Long userId;
    private String sessionId;
    private String taskType;
    private String taskDescription;
    private Integer status;
    private String statusDesc;
    private String resultSummary;
    private String errorMessage;
    private Long durationMs;
    private Integer nodeCount;
    private LocalDateTime createTime;
}

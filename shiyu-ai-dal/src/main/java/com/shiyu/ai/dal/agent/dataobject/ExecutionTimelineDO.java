package com.shiyu.ai.dal.agent.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("execution_timeline")
public class ExecutionTimelineDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

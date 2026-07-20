package com.shiyu.ai.dal.memory.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Table("episodic_memory")
public class EpisodicMemoryDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String executionId;
    private String agentId;
    private Long userId;
    private String sessionId;
    private String taskType;
    private String taskDescription;
    private String status;
    private String resultSummary;
    private String errorMessage;
    private Long durationMs;
    private Integer nodeCount;
    private LocalDateTime createTime;
}

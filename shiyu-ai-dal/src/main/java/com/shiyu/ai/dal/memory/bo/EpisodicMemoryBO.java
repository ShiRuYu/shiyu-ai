package com.shiyu.ai.dal.memory.bo;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.github.linpeilie.annotations.AutoMapper;
import com.shiyu.ai.dal.memory.dataobject.EpisodicMemoryDO;

@Data
@AutoMapper(target = EpisodicMemoryDO.class, reverseConvertGenerate = true)
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
    private String status;
    private String resultSummary;
    private String errorMessage;
    private Long durationMs;
    private Integer nodeCount;
    private LocalDateTime createTime;
}

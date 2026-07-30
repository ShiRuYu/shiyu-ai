package com.shiyu.ai.dal.memory.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 情景记忆（Agent执行历史）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("memory_episodic_memory")
public class EpisodicMemoryDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String executionId;
    private String agentId;
    private Long userId;
    private String sessionId;
    private String taskType;
    private String taskDescription;
    private String resultSummary;
    private String errorMessage;
    private Long durationMs;
    private Integer nodeCount;
    private LocalDateTime createTime;
}

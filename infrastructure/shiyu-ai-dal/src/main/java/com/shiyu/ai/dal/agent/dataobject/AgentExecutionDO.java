package com.shiyu.ai.dal.agent.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * Agent 执行记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("agent_execution")
@AutoMapper(target = AgentExecutionBO.class, reverseConvertGenerate = true)
public class AgentExecutionDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String executionId;

    private String agentId;

    private String version;

    private Long userId;

    private String sessionId;

    private String nodeId;

    private String nodeType;

    private String inputData;

    private String outputData;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

}

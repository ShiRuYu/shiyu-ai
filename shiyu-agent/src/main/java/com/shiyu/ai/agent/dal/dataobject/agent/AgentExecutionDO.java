package com.shiyu.ai.agent.dal.dataobject.agent;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("agent_execution")
public class AgentExecutionDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

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

    private String status;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

}

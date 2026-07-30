package com.shiyu.ai.dal.agent.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 节点执行记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("agent_node_execution")
public class NodeExecutionDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String executionId;

    private String nodeId;

    private String nodeType;

    private String inputData;

    private String outputData;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private Integer retryCount;

    private LocalDateTime createTime;
}

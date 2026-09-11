package com.shiyu.ai.agent.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.agent.implementation.domain.model.NodeExecutionBO;
import com.shiyu.ai.common.mybatis.model.TenantEntity;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 节点执行记录 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("agent_node_execution")
@AutoMapper(target = NodeExecutionBO.class, reverseConvertGenerate = true)
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

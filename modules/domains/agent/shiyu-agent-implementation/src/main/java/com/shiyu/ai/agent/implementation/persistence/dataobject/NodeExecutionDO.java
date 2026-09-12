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

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private String executionId;

    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeId;

    /**
     * nodeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeType;

    /**
     * 输入数据，表示当前对象中的对应属性。
     */
    private String inputData;

    /**
     * 输出数据，表示当前对象中的对应属性。
     */
    private String outputData;

    /**
     * 错误消息，表示当前对象中的对应属性。
     */
    private String errorMessage;

    /**
     * startTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime startTime;

    /**
     * endTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime endTime;

    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long durationMs;

    /**
     * retryCount 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer retryCount;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}

package com.shiyu.ai.agent.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.common.mybatis.model.TenantEntity;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Agent 执行记录 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("agent_execution")
@AutoMapper(target = AgentExecutionBO.class, reverseConvertGenerate = true)
public class AgentExecutionDO extends TenantEntity {

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
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;

    /**
     * 版本，表示当前对象中的对应属性。
     */
    private String version;

    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;

    /**
     * sessionId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String sessionId;

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
}

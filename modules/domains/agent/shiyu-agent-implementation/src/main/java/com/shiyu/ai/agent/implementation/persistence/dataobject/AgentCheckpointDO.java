package com.shiyu.ai.agent.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.agent.implementation.domain.model.AgentCheckpointBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {@code AgentCheckpointDO} 是智能体模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("agent_checkpoint")
@AutoMapper(target = AgentCheckpointBO.class, reverseConvertGenerate = true)
public class AgentCheckpointDO implements Serializable {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;

    /**
     * checkpointId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String checkpointId;

    /**
     * 执行标识，表示当前对象中的对应属性。
     */
    private String executionId;

    /**
     * nodeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String nodeId;

    /**
     * 状态数据，表示当前对象中的对应属性。
     */
    private String stateData;

    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}

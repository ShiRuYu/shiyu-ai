package com.shiyu.ai.agent.implementation.domain.model;

import com.shiyu.ai.common.core.domain.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/** Agent 定义业务对象 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentDefBO extends BaseEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * agentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String agentId;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * ownerId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long ownerId;
    /**
     * currentVersion 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String currentVersion;

    /** 扩展字段：聚合的节点入参定义 JSON */
    private String extInfo;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}

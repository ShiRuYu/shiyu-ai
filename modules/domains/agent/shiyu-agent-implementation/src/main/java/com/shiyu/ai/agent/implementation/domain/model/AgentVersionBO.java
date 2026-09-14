package com.shiyu.ai.agent.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** Agent 版本业务对象 */
@Data
public class AgentVersionBO implements Serializable {

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
     * versionNumber 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String versionNumber;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private String description;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * statusDesc 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String statusDesc;
    /**
     * 图结构配置，表示当前对象中的对应属性。
     */
    private String graphConfig;
    /**
     * canvasConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String canvasConfig;

    /** 扩展字段：版本所有节点的入参定义 JSON */
    private String extInfo;

    /**
     * delFlag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String delFlag;
    /**
     * createBy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String createBy;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
    /**
     * updateBy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String updateBy;
    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;
}

package com.shiyu.ai.agent.implementation.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表示 Audit Log 领域对象的业务状态和属性。
 */
@Data
public class AuditLogBO implements Serializable {

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
     * 用户标识，表示当前对象中的对应属性。
     */
    private Long userId;
    /**
     * action 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String action;
    /**
     * 目标类型，表示当前对象中的对应属性。
     */
    private String targetType;
    /**
     * 目标标识，表示当前对象中的对应属性。
     */
    private String targetId;
    /**
     * detail 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String detail;
    /**
     * ip 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String ip;
    /**
     * 结果，表示当前对象中的对应属性。
     */
    private String result;
    /**
     * errorMsg 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String errorMsg;
    /**
     * durationMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long durationMs;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
}

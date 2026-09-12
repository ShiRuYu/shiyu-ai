package com.shiyu.ai.agent.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * {@code AuditLogDO} 是智能体模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("observation_audit_log")
@AutoMapper(target = AuditLogBO.class, reverseConvertGenerate = true)
public class AuditLogDO implements Serializable {

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

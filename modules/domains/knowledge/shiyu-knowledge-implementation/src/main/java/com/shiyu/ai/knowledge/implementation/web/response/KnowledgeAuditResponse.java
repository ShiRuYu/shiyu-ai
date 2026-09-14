package com.shiyu.ai.knowledge.implementation.web.response;

import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeAuditLogBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * KnowledgeAuditResponse 数据对象，承载知识领域相关业务数据。
 */
@Data
@AutoMapper(target = KnowledgeAuditLogBO.class)
public class KnowledgeAuditResponse {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private Long tenantId;
    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * 资源类型，表示当前对象中的对应属性。
     */
    private String resourceType;
    /**
     * 资源标识，表示当前对象中的对应属性。
     */
    private Long resourceId;
    /**
     * action 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String action;
    /**
     * detailJson 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String detailJson;
    /**
     * 状态，表示当前对象中的对应属性。
     */
    private Integer status;
    /**
     * delFlag 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer delFlag;
    /**
     * createTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime createTime;
    /**
     * updateTime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime updateTime;
}

package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code KnowledgeAuditLogBO} 是知识模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeAuditLogBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
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
}

package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表示 知识 关系 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeRelationBO extends TenantModel {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * 来源标识，表示当前对象中的对应属性。
     */
    private Long sourceId;
    /**
     * 目标标识，表示当前对象中的对应属性。
     */
    private Long targetId;
    /**
     * relationType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String relationType;
    /**
     * 权重，表示当前对象中的对应属性。
     */
    private Double weight;

    /** 状态（依据业务灵活定义） */
    private Integer status;
}

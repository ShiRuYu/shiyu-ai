package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.foundation.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表示 知识 空间 Member 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeSpaceMemberBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * principalType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String principalType;
    /**
     * principalId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long principalId;
    /**
     * spaceRole 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String spaceRole;
}

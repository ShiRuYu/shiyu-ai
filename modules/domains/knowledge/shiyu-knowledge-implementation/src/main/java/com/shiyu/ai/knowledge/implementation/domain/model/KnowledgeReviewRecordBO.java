package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.foundation.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表示 知识 复习 Record 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeReviewRecordBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * documentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long documentId;
    /**
     * 版本标识，表示当前对象中的对应属性。
     */
    private Long versionId;
    /**
     * action 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String action;
    /**
     * commentText 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String commentText;
}

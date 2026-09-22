package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.foundation.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表示 知识 Evaluation Case 领域对象的业务状态和属性。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeEvaluationCaseBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * 题目，表示当前对象中的对应属性。
     */
    private String question;
    /**
     * expectedDocIds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String expectedDocIds;
    /**
     * expectedAnswer 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String expectedAnswer;
}

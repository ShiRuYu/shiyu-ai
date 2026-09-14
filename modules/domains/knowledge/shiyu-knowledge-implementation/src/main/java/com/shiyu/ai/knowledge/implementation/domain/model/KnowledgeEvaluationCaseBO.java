package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * {@code KnowledgeEvaluationCaseBO} 是知识模块的业务对象，承载用例处理所需的领域数据。
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

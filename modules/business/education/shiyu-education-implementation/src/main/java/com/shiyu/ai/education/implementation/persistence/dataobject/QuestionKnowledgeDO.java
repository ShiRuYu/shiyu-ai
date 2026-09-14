package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.QuestionKnowledgeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * {@code QuestionKnowledgeDO} 是教育模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@Table("edu_question_knowledge")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionKnowledgeBO.class, reverseConvertGenerate = true)
public class QuestionKnowledgeDO extends TenantEntity {

    /**
     * serialVersionUID 属性，保存当前对象中的业务数据或协作依赖。
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 题目标识，表示当前对象中的对应属性。
     */
    private Long questionId;
    /**
     * knowledgeId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long knowledgeId;
    /**
     * 权重，表示当前对象中的对应属性。
     */
    private Double weight;
}

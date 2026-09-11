package com.shiyu.ai.education.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.education.implementation.domain.model.QuestionKnowledgeBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@Table("edu_question_knowledge")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionKnowledgeBO.class, reverseConvertGenerate = true)
public class QuestionKnowledgeDO extends TenantEntity {

    @Serial private static final long serialVersionUID = 1L;

    private Long questionId;
    private Long knowledgeId;
    private Double weight;
}

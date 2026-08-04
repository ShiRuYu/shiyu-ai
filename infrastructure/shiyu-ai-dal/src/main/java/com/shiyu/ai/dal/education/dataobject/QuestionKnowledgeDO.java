package com.shiyu.ai.dal.education.dataobject;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import com.shiyu.ai.education.domain.model.QuestionKnowledgeBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@Table("edu_question_knowledge")
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionKnowledgeBO.class, reverseConvertGenerate = true)
public class QuestionKnowledgeDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long questionId;
    private Long knowledgeId;
    private Double weight;
}

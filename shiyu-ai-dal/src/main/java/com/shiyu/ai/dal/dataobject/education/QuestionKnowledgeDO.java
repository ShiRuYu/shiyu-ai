package com.shiyu.ai.dal.dataobject.education;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@Table("edu_question_knowledge")
@EqualsAndHashCode(callSuper = true)
public class QuestionKnowledgeDO extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long questionId;
    private Long knowledgeId;
    private Double weight;
}

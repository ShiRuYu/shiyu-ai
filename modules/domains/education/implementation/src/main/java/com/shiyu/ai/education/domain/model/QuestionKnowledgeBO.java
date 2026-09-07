package com.shiyu.ai.education.domain.model;
import lombok.Data;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionKnowledgeBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long questionId;
    private Long knowledgeId;
    private Double weight;
}

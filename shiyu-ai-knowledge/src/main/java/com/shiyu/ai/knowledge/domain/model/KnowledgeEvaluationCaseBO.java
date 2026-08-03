package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeEvaluationCaseBO extends TenantModel {
    private Long id;
    private Long spaceId;
    private String question;
    private String expectedDocIds;
    private String expectedAnswer;
}

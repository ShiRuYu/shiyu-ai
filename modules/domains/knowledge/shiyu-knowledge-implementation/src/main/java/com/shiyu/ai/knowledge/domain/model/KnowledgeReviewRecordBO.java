package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeReviewRecordBO extends TenantModel {
    private Long id;
    private Long documentId;
    private Long versionId;
    private String action;
    private String commentText;
}

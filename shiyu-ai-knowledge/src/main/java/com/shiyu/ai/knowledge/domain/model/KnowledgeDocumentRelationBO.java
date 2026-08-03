package com.shiyu.ai.knowledge.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocumentRelationBO extends TenantModel {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long spaceId;
    private Long sourceDocumentId;
    private Long targetDocumentId;
    private String relationType;
}

package com.shiyu.ai.education.domain.model;
import lombok.Data;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceKnowledgeBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long resourceId;
    private Long knowledgeId;
    private Integer sortOrder;
}

package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeAuditLogBO extends TenantModel {
    private Long id;
    private Long spaceId;
    private String resourceType;
    private Long resourceId;
    private String action;
    private String detailJson;
}

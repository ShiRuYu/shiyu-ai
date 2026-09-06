package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeSpaceMemberBO extends TenantModel {
    private Long id;
    private Long spaceId;
    private String principalType;
    private Long principalId;
    private String spaceRole;
}

package com.shiyu.ai.knowledge.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeRelationBO extends TenantModel {

    private Long id;
    private Long spaceId;
    private Long sourceId;
    private Long targetId;
    private String relationType;
    private Double weight;
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}

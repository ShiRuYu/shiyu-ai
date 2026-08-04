package com.shiyu.ai.knowledge.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDocRelationBO extends TenantModel {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long spaceId;
    private Long docId;
    private Long knowledgeId;
    private String relationType;
    private LocalDateTime createTime;
}

package com.shiyu.ai.dal.knowledge.bo;

import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentRelationDO;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = KnowledgeDocumentRelationDO.class, reverseConvertGenerate = true)
public class KnowledgeDocumentRelationBO extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long spaceId;
    private Long sourceDocumentId;
    private Long targetDocumentId;
    private String relationType;
}

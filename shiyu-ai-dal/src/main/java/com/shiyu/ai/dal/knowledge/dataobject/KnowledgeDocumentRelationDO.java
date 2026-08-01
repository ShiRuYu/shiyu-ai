package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_document_relation")
public class KnowledgeDocumentRelationDO extends TenantEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long spaceId;
    private Long sourceDocumentId;
    private Long targetDocumentId;
    private String relationType;
}

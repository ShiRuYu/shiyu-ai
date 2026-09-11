package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentRelationBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_document_relation")
@AutoMapper(target = KnowledgeDocumentRelationBO.class, reverseConvertGenerate = true)
public class KnowledgeDocumentRelationDO extends TenantEntity {
    @Serial private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long spaceId;
    private Long sourceDocumentId;
    private Long targetDocumentId;
    private String relationType;
}

package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_document")
@AutoMapper(target = KnowledgeDocumentBO.class, reverseConvertGenerate = true)
public class KnowledgeDocumentDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long spaceId;
    private Long currentVersionId;
    private String title;
    private String content;
    private String docType;
    private String source;
    private String author;
    private String lifecycleStatus;
    private String parseStatus;
    private String storageProvider;
    private Long storageObjectId;
    private String objectKey;
    private String mimeType;
    private Long fileSize;
    private String checksum;
}


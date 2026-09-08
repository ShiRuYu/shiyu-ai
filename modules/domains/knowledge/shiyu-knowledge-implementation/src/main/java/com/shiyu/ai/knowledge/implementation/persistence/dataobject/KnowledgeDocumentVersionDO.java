package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import io.github.linpeilie.annotations.AutoMapper;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_document_version")
@AutoMapper(target = KnowledgeDocumentVersionBO.class, reverseConvertGenerate = true)
public class KnowledgeDocumentVersionDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long documentId;
    private Long spaceId;
    private Integer versionNo;
    private String title;
    private String content;
    private String storageProvider;
    private Long storageObjectId;
    private String objectKey;
    private String mimeType;
    private Long fileSize;
    private String checksum;
    private String lifecycleStatus;
    private String parseStatus;
    private String modelProfile;
    private LocalDateTime publishedAt;
}


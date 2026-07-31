package com.shiyu.ai.dal.knowledge.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Table("knowledge_document_version")
public class KnowledgeDocumentVersionDO extends TenantEntity {

    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long documentId;
    private Long spaceId;
    private Integer versionNo;
    private String title;
    private String content;
    private String storageProvider;
    private String objectKey;
    private String mimeType;
    private Long fileSize;
    private String checksum;
    private String lifecycleStatus;
    private String parseStatus;
    private String modelProfile;
    private LocalDateTime publishedAt;
}

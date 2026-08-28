package com.shiyu.ai.knowledge.domain.model;
import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeDocumentVersionBO extends TenantModel {
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

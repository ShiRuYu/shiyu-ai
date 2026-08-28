package com.shiyu.ai.knowledge.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeDocumentBO extends TenantModel {

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
    /**
     * 状态（依据业务灵活定义）
     */
    private Integer status;

}

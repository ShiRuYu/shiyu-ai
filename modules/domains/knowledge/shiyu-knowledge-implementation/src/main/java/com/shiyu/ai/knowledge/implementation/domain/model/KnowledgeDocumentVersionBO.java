package com.shiyu.ai.knowledge.implementation.domain.model;

import com.shiyu.ai.common.core.domain.TenantModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code KnowledgeDocumentVersionBO} 是知识模块的业务对象，承载用例处理所需的领域数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class KnowledgeDocumentVersionBO extends TenantModel {
    /**
     * 标识，表示当前对象中的对应属性。
     */
    private Long id;
    /**
     * documentId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long documentId;
    /**
     * spaceId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long spaceId;
    /**
     * versionNo 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Integer versionNo;
    /**
     * 标题，表示当前对象中的对应属性。
     */
    private String title;
    /**
     * 内容，表示当前对象中的对应属性。
     */
    private String content;
    /**
     * storageProvider 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String storageProvider;
    /**
     * storageObjectId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private Long storageObjectId;
    /**
     * objectKey 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String objectKey;
    /**
     * mimeType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String mimeType;
    /**
     * 文件大小，表示当前对象中的对应属性。
     */
    private Long fileSize;
    /**
     * checksum 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String checksum;
    /**
     * lifecycleStatus 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String lifecycleStatus;
    /**
     * parseStatus 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String parseStatus;
    /**
     * modelProfile 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String modelProfile;
    /**
     * publishedAt 属性，保存当前对象中的业务数据或协作依赖。
     */
    private LocalDateTime publishedAt;
}

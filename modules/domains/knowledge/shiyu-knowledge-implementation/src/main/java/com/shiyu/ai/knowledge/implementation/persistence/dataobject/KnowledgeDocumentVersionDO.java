package com.shiyu.ai.knowledge.implementation.persistence.dataobject;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.shiyu.ai.common.mybatis.model.TenantEntity;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentVersionBO;

import io.github.linpeilie.annotations.AutoMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * {@code KnowledgeDocumentVersionDO} 是知识模块的持久化对象，承载数据库记录与映射字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
@Table("knowledge_document_version")
@AutoMapper(target = KnowledgeDocumentVersionBO.class, reverseConvertGenerate = true)
public class KnowledgeDocumentVersionDO extends TenantEntity {

    /**
     * 标识，表示当前对象中的对应属性。
     */
    @Id(keyType = KeyType.Auto)
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

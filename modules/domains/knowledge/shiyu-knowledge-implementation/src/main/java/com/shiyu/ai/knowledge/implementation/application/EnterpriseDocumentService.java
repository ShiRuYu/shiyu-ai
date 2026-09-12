package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * EnterpriseDocumentService 服务接口，负责执行知识领域相关业务操作。
 */
public interface EnterpriseDocumentService {

    /**
     * 执行 {@code page} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param spaceId 方法参数。
     * @param pageNum 页码。
     * @param pageSize 分页大小。
     * @param keyword 方法参数。
     * @param lifecycleStatus 方法参数。
     * @param parseStatus 方法参数。
     *
     * @return 操作结果。
     */
    PageData<DocumentView> page(
            ActorContext actor,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String lifecycleStatus,
            String parseStatus);

    /**
     * 根据标识查询对应的数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     *
     * @return 操作结果。
     */
    DocumentView get(ActorContext actor, Long documentId);

    /**
     * 创建并保存业务对象。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    UploadResult registerStoredFile(ActorContext actor, StoredFileRequest request);

    /**
     * 执行 {@code versions} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<VersionView> versions(ActorContext actor, Long documentId);

    /**
     * 执行 {@code submit} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param comment 方法参数。
     *
     * @return 操作结果。
     */
    DocumentView submit(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 {@code approve} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param comment 方法参数。
     *
     * @return 操作结果。
     */
    DocumentView approve(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 {@code reject} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param comment 方法参数。
     *
     * @return 操作结果。
     */
    DocumentView reject(ActorContext actor, Long documentId, String comment);

    /**
     * 发布或发送业务事件。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param comment 方法参数。
     *
     * @return 操作结果。
     */
    DocumentView publish(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 {@code archive} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param comment 方法参数。
     *
     * @return 操作结果。
     */
    DocumentView archive(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 {@code rollback} 定义的接口操作。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     * @param versionId 方法参数。
     *
     * @return 操作结果。
     */
    DocumentView rollback(ActorContext actor, Long documentId, Long versionId);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param actor 当前操作主体上下文。
     * @param documentId 方法参数。
     */
    void delete(ActorContext actor, Long documentId);

    /**
     * {@code StoredFileRequest} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param title 标题，表示该记录组件承载的数据。
     * @param originalName originalName 属性，表示该记录组件承载的数据。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param storageProvider storageProvider 属性，表示该记录组件承载的数据。
     * @param mimeType mimeType 属性，表示该记录组件承载的数据。
     * @param fileSize fileSize 属性，表示该记录组件承载的数据。
     * @param checksum checksum 属性，表示该记录组件承载的数据。
     */
    record StoredFileRequest(
            @NotNull Long spaceId,
            @NotBlank String title,
            @NotBlank String originalName,
            @NotBlank String objectKey,
            String storageProvider,
            String mimeType,
            long fileSize,
            @NotBlank String checksum) {}

    /**
     * {@code UploadResult} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param document document 属性，表示该记录组件承载的数据。
     * @param versionId versionId 属性，表示该记录组件承载的数据。
     * @param jobId jobId 属性，表示该记录组件承载的数据。
     * @param duplicate duplicate 属性，表示该记录组件承载的数据。
     */
    record UploadResult(DocumentView document, Long versionId, Long jobId, boolean duplicate) {}

    /**
     * {@code DocumentView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param currentVersionId currentVersionId 属性，表示该记录组件承载的数据。
     * @param title 标题，表示该记录组件承载的数据。
     * @param docType docType 属性，表示该记录组件承载的数据。
     * @param source 来源，表示该记录组件承载的数据。
     * @param lifecycleStatus lifecycleStatus 属性，表示该记录组件承载的数据。
     * @param parseStatus parseStatus 属性，表示该记录组件承载的数据。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param mimeType mimeType 属性，表示该记录组件承载的数据。
     * @param fileSize fileSize 属性，表示该记录组件承载的数据。
     * @param checksum checksum 属性，表示该记录组件承载的数据。
     * @param createTime createTime 属性，表示该记录组件承载的数据。
     * @param updateTime updateTime 属性，表示该记录组件承载的数据。
     */
    record DocumentView(
            Long id,
            Long spaceId,
            Long currentVersionId,
            String title,
            String docType,
            String source,
            String lifecycleStatus,
            String parseStatus,
            String objectKey,
            String mimeType,
            Long fileSize,
            String checksum,
            LocalDateTime createTime,
            LocalDateTime updateTime) {}

    /**
     * {@code VersionView} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param documentId documentId 属性，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param versionNo versionNo 属性，表示该记录组件承载的数据。
     * @param title 标题，表示该记录组件承载的数据。
     * @param lifecycleStatus lifecycleStatus 属性，表示该记录组件承载的数据。
     * @param parseStatus parseStatus 属性，表示该记录组件承载的数据。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param mimeType mimeType 属性，表示该记录组件承载的数据。
     * @param fileSize fileSize 属性，表示该记录组件承载的数据。
     * @param checksum checksum 属性，表示该记录组件承载的数据。
     * @param modelProfile modelProfile 属性，表示该记录组件承载的数据。
     * @param publishedAt publishedAt 属性，表示该记录组件承载的数据。
     * @param createTime createTime 属性，表示该记录组件承载的数据。
     */
    record VersionView(
            Long id,
            Long documentId,
            Long spaceId,
            Integer versionNo,
            String title,
            String lifecycleStatus,
            String parseStatus,
            String objectKey,
            String mimeType,
            Long fileSize,
            String checksum,
            String modelProfile,
            LocalDateTime publishedAt,
            LocalDateTime createTime) {}
}

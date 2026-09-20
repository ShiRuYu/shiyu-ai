package com.shiyu.ai.knowledge.implementation.application;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.kernel.context.ActorContext;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提供 Enterprise 文档 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
public interface EnterpriseDocumentService {

    /**
     * 查询 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param spaceId 用于定位space的标识。
     * @param pageNum 用于完成本次业务处理的 pageNum 参数。
     * @param pageSize 每页返回的数据数量。
     * @param keyword 用于完成本次业务处理的 keyword 参数。
     * @param lifecycleStatus 用于完成本次业务处理的 lifecycleStatus 参数。
     * @param parseStatus 用于完成本次业务处理的 parseStatus 参数。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
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
     * 查询 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    DocumentView get(ActorContext actor, Long documentId);

    /**
     * 创建或保存 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    UploadResult registerStoredFile(ActorContext actor, StoredFileRequest request);

    /**
     * 执行 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<VersionView> versions(ActorContext actor, Long documentId);

    /**
     * 执行 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param comment 用于完成本次业务处理的 comment 参数。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    DocumentView submit(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param comment 用于完成本次业务处理的 comment 参数。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    DocumentView approve(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param comment 用于完成本次业务处理的 comment 参数。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    DocumentView reject(ActorContext actor, Long documentId, String comment);

    /**
     * 发布或发送 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param comment 用于完成本次业务处理的 comment 参数。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    DocumentView publish(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param comment 用于完成本次业务处理的 comment 参数。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    DocumentView archive(ActorContext actor, Long documentId, String comment);

    /**
     * 执行 Enterprise 文档 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     * @param versionId 用于定位version的标识。
     * @return 返回 Enterprise 文档 相关操作生成的结果数据。
     */
    DocumentView rollback(ActorContext actor, Long documentId, Long versionId);

    /**
     * 删除或移除 Enterprise 文档 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param documentId 用于定位document的标识。
     */
    void delete(ActorContext actor, Long documentId);

    /**
     * 封装 Stored 文件 相关的不可变数据及其字段约束。
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
     * 返回文档上传后的文档、版本、摄取任务及重复上传标志。
     */
    record UploadResult(DocumentView document, Long versionId, Long jobId, boolean duplicate) {}

    /**
     * 封装 文档 View 相关的不可变数据及其字段约束。
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
     * 封装 Version View 相关的不可变数据及其字段约束。
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

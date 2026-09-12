package com.shiyu.ai.knowledge.implementation.application.service;


import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.DocumentView;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService.SpaceRole;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.StoredFileRequest;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.UploadResult;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService.VersionView;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.tx.TransactionHookExecutor;
import com.shiyu.ai.common.core.tx.TransactionTemplateExecutor;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.knowledge.implementation.application.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeAuditService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.implementation.application.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.implementation.application.rag.DocumentIngestionService;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeReviewRecordBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.implementation.domain.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.implementation.infrastructure.index.service.KnowledgeIndexService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * {@code EnterpriseDocumentServiceImpl} 实现知识模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDocumentServiceImpl implements EnterpriseDocumentService {

    /**
     * documentRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeDocumentRepository documentRepository;
    /**
     * enterpriseRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    /**
     * spaceService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeSpaceService spaceService;
    /**
     * 审计服务，表示当前对象中的对应属性。
     */
    private final KnowledgeAuditService auditService;
    /**
     * documentRelationService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KnowledgeDocumentRelationService documentRelationService;
    /**
     * ingestionService 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final DocumentIngestionService ingestionService;
    /**
     * 索引服务，表示当前对象中的对应属性。
     */
    private final KnowledgeIndexService indexService;
    /**
     * transactionTemplateExecutor 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TransactionTemplateExecutor transactionTemplateExecutor;
    /**
     * storageMetadataStore 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final StorageMetadataStore storageMetadataStore;
    /**
     * objectStorage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ObjectStorage objectStorage;

    /**
     * {@code page} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param pageNum 参数值，用于执行当前操作。
     * @param pageSize 参数值，用于执行当前操作。
     * @param keyword 参数值，用于执行当前操作。
     * @param lifecycleStatus 参数值，用于执行当前操作。
     * @param parseStatus 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public PageData<DocumentView> page(
            ActorContext actor,
            Long spaceId,
            int pageNum,
            int pageSize,
            String keyword,
            String lifecycleStatus,
            String parseStatus) {
        requireActor(actor);
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        PageData<KnowledgeDocumentBO> result =
                documentRepository.pageBySpace(
                        actor.tenantId(),
                        spaceId,
                        pageNum,
                        pageSize,
                        keyword,
                        lifecycleStatus,
                        parseStatus);
        return new PageData<>(
                result.getItems().stream().map(this::toView).toList(), result.getTotal());
    }

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public DocumentView get(ActorContext actor, Long documentId) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        spaceService.requireAccess(
                document.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return toView(document);
    }

    /**
     * {@code registerStoredFile} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult registerStoredFile(ActorContext actor, StoredFileRequest request) {
        requireActor(actor);
        spaceService.requireAccess(
                request.spaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        KnowledgeDocumentBO duplicate =
                documentRepository.findBySpaceAndChecksum(
                        actor.tenantId(), request.spaceId(), request.checksum());
        if (duplicate != null) {
            return new UploadResult(toView(duplicate), duplicate.getCurrentVersionId(), null, true);
        }

        KnowledgeDocumentBO document = new KnowledgeDocumentBO();
        document.setSpaceId(request.spaceId());
        document.setTitle(request.title());
        document.setContent("");
        document.setDocType(extension(request.originalName()));
        document.setSource("UPLOAD");
        document.setLifecycleStatus("DRAFT");
        document.setParseStatus("PENDING");
        document.setStorageProvider(defaultText(request.storageProvider(), "local"));
        Long tenantId = actor.tenantId().value();
        Long storageObjectId =
                storageMetadataStore
                        .findObjectByKey(tenantId, request.objectKey())
                        .map(StorageMetadataStore.StorageObjectRecord::id)
                        .orElse(null);
        document.setStorageObjectId(storageObjectId);
        document.setObjectKey(request.objectKey());
        document.setMimeType(request.mimeType());
        document.setFileSize(request.fileSize());
        document.setChecksum(request.checksum());
        document.setStatus(1);
        document.setDelFlag(0);
        requireWrite(documentRepository.insert(actor.tenantId(), document), "注册知识文档");

        KnowledgeSpaceBO space =
                enterpriseRepository.findSpace(actor.tenantId(), request.spaceId());
        KnowledgeDocumentVersionBO version = new KnowledgeDocumentVersionBO();
        version.setDocumentId(document.getId());
        version.setSpaceId(request.spaceId());
        version.setVersionNo(1);
        version.setTitle(request.title());
        version.setStorageProvider(document.getStorageProvider());
        version.setObjectKey(request.objectKey());
        version.setMimeType(request.mimeType());
        version.setFileSize(request.fileSize());
        version.setChecksum(request.checksum());
        version.setLifecycleStatus("DRAFT");
        version.setParseStatus("PENDING");
        version.setModelProfile(space == null ? "default" : space.getEmbeddingProfile());
        version.setStorageObjectId(storageObjectId);
        version.setDelFlag(0);
        enterpriseRepository.insertVersion(actor.tenantId(), version);

        document.setCurrentVersionId(version.getId());
        documentRepository.update(actor.tenantId(), document);

        String jobKey = "INGEST:" + request.spaceId() + ":" + request.checksum();
        KnowledgeIngestionJobBO job = enterpriseRepository.findJobByKey(actor.tenantId(), jobKey);
        if (job == null) {
            job = new KnowledgeIngestionJobBO();
            job.setJobKey(jobKey);
            job.setJobType("INGEST");
            job.setSpaceId(request.spaceId());
            job.setDocumentId(document.getId());
            job.setVersionId(version.getId());
            job.setActorUserId(actor.userId().value());
            job.setJobStatus("PENDING");
            job.setStage("QUEUED");
            job.setProgress(0);
            job.setAttempts(0);
            job.setMaxAttempts(3);
            job.setLockVersion(0L);
            job.setDelFlag(0);
            enterpriseRepository.insertJob(actor.tenantId(), job);
        } else {
            KnowledgeDocumentBO previous =
                    job.getDocumentId() == null
                            ? null
                            : documentRepository.selectById(actor.tenantId(), job.getDocumentId());
            if (previous == null || Integer.valueOf(1).equals(previous.getDelFlag())) {
                job.setDocumentId(document.getId());
                job.setVersionId(version.getId());
                job.setActorUserId(actor.userId().value());
                job.setJobStatus("PENDING");
                job.setStage("QUEUED");
                job.setProgress(0);
                job.setAttempts(0);
                job.setErrorMessage(null);
                job.setHeartbeatTime(null);
                job.setStartedTime(null);
                job.setFinishedTime(null);
                job.setDelFlag(0);
                enterpriseRepository.updateJob(actor.tenantId(), job);
            }
        }
        auditService.record(
                actor, request.spaceId(), "DOCUMENT", document.getId(), "UPLOAD", request);
        return new UploadResult(toView(document), version.getId(), job.getId(), false);
    }

    /**
     * {@code versions} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<VersionView> versions(ActorContext actor, Long documentId) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        spaceService.requireAccess(
                document.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER, actor);
        return enterpriseRepository.findVersions(actor.tenantId(), documentId).stream()
                .map(this::toVersionView)
                .toList();
    }

    /**
     * {@code submit} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView submit(ActorContext actor, Long documentId, String comment) {
        return transition(
                actor,
                documentId,
                "DRAFT",
                "REVIEWING",
                "SUBMIT",
                KnowledgeSpaceService.SpaceRole.EDITOR,
                comment);
    }

    /**
     * {@code approve} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView approve(ActorContext actor, Long documentId, String comment) {
        return transition(
                actor,
                documentId,
                "REVIEWING",
                "PUBLISHED",
                "APPROVE",
                KnowledgeSpaceService.SpaceRole.REVIEWER,
                comment);
    }

    /**
     * {@code reject} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView reject(ActorContext actor, Long documentId, String comment) {
        return transition(
                actor,
                documentId,
                "REVIEWING",
                "DRAFT",
                "REJECT",
                KnowledgeSpaceService.SpaceRole.REVIEWER,
                comment);
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView publish(ActorContext actor, Long documentId, String comment) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        KnowledgeSpaceBO space =
                enterpriseRepository.findSpace(actor.tenantId(), document.getSpaceId());
        if (space != null
                && "REQUIRED".equals(space.getBindingMode())
                && documentRelationService.listPointIds(actor, documentId).isEmpty()) {
            throw new ServiceException("当前知识空间要求文档至少关联一个知识点后才能发布");
        }
        KnowledgeSpaceService.SpaceRole required =
                space != null && "REQUIRED".equals(space.getReviewMode())
                        ? KnowledgeSpaceService.SpaceRole.REVIEWER
                        : KnowledgeSpaceService.SpaceRole.EDITOR;
        return transition(actor, documentId, null, "PUBLISHED", "PUBLISH", required, comment);
    }

    /**
     * {@code archive} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView archive(ActorContext actor, Long documentId, String comment) {
        return transition(
                actor,
                documentId,
                "PUBLISHED",
                "ARCHIVED",
                "ARCHIVE",
                KnowledgeSpaceService.SpaceRole.EDITOR,
                comment);
    }

    /**
     * {@code rollback} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     * @param versionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView rollback(ActorContext actor, Long documentId, Long versionId) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        spaceService.requireAccess(
                document.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR, actor);
        boolean wasPublished = "PUBLISHED".equals(document.getLifecycleStatus());
        KnowledgeDocumentVersionBO source =
                enterpriseRepository.findVersion(actor.tenantId(), versionId);
        if (source == null || !documentId.equals(source.getDocumentId())) {
            throw new ServiceException("文档版本不存在");
        }
        KnowledgeDocumentVersionBO version = copyVersion(source);
        version.setVersionNo(enterpriseRepository.nextVersionNo(actor.tenantId(), documentId));
        version.setLifecycleStatus("DRAFT");
        version.setPublishedAt(null);
        enterpriseRepository.insertVersion(actor.tenantId(), version);
        document.setCurrentVersionId(version.getId());
        document.setTitle(version.getTitle());
        document.setLifecycleStatus("DRAFT");
        document.setParseStatus(version.getParseStatus());
        document.setObjectKey(version.getObjectKey());
        document.setChecksum(version.getChecksum());
        documentRepository.update(actor.tenantId(), document);
        auditService.record(
                actor, document.getSpaceId(), "DOCUMENT", documentId, "ROLLBACK", versionId);
        if (wasPublished) {
            scheduleIndexRebuild(actor.tenantId(), document.getSpaceId());
        }
        return toView(document);
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     */
    @Override
    public void delete(ActorContext actor, Long documentId) {
        requireActor(actor);
        DeletionContext deletion =
                transactionTemplateExecutor.execute(
                        new DefaultTransactionDefinition(),
                        () -> {
                            KnowledgeDocumentBO document = requireDocument(actor, documentId);
                            spaceService.requireAccess(
                                    document.getSpaceId(),
                                    KnowledgeSpaceService.SpaceRole.EDITOR,
                                    actor);
                            documentRelationService.removeDocumentRelations(actor, documentId);
                            ingestionService.delete(
                                    new com.shiyu.ai.kernel.context.TenantId(
                                            document.getTenantId()),
                                    documentId);
                            documentRepository.deleteById(actor.tenantId(), documentId);
                            auditService.record(
                                    actor,
                                    document.getSpaceId(),
                                    "DOCUMENT",
                                    documentId,
                                    "DELETE",
                                    null);
                            return new DeletionContext(
                                    document.getTenantId(),
                                    document.getSpaceId(),
                                    document.getObjectKey());
                        });
        if (deletion.objectKey() != null && !deletion.objectKey().isBlank()) {
            boolean physicalDeleted = false;
            try {
                objectStorage.delete(deletion.objectKey());
                physicalDeleted = true;
            } catch (IOException exception) {
                log.warn(
                        "文档记录已删除，但物理对象删除失败，等待存储一致性任务处理，objectKeyLength={}",
                        deletion.objectKey() == null ? 0 : deletion.objectKey().length(),
                        exception);
            }
            if (physicalDeleted && deletion.tenantId() != null) {
                storageMetadataStore.markObjectDeleted(deletion.tenantId(), deletion.objectKey());
            }
        }
        if (deletion.tenantId() != null && deletion.spaceId() != null) {
            try {
                indexService.rebuild(
                        new com.shiyu.ai.kernel.context.TenantId(deletion.tenantId()),
                        deletion.spaceId());
            } catch (RuntimeException exception) {
                log.error(
                        "Document deleted but space index rebuild failed: documentIdPresent={},"
                                + " spaceIdPresent={}, errorType={}, errorMessageLength={}",
                        documentId != null,
                        deletion.spaceId() != null,
                        exception.getClass().getSimpleName(),
                        exception.getMessage() == null ? 0 : exception.getMessage().length());
                throw new ServiceException("文档已删除，但空间索引重建失败，请稍后在索引任务中重试");
            }
        }
    }

    /**
     * {@code DeletionContext} 封装知识模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     */
    private record DeletionContext(Long tenantId, Long spaceId, String objectKey) {}

    /**
     * {@code transition} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param documentId 参数值，用于执行当前操作。
     * @param expected 参数值，用于执行当前操作。
     * @param target 参数值，用于执行当前操作。
     * @param action 参数值，用于执行当前操作。
     * @param role 参数值，用于执行当前操作。
     * @param comment 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    protected DocumentView transition(
            ActorContext actor,
            Long documentId,
            String expected,
            String target,
            String action,
            KnowledgeSpaceService.SpaceRole role,
            String comment) {
        KnowledgeDocumentBO document = requireDocument(actor, documentId);
        spaceService.requireAccess(document.getSpaceId(), role, actor);
        if (expected != null && !expected.equals(document.getLifecycleStatus())) {
            throw new ServiceException("当前状态不允许执行该操作: " + document.getLifecycleStatus());
        }
        if ("PUBLISHED".equals(target) && !"READY".equals(document.getParseStatus())) {
            throw new ServiceException("文档尚未解析完成，不能发布");
        }
        document.setLifecycleStatus(target);
        documentRepository.update(actor.tenantId(), document);
        KnowledgeDocumentVersionBO version =
                enterpriseRepository.findVersion(actor.tenantId(), document.getCurrentVersionId());
        if (version != null) {
            version.setLifecycleStatus(target);
            if ("PUBLISHED".equals(target)) {
                version.setPublishedAt(LocalDateTime.now());
            }
            enterpriseRepository.updateVersion(actor.tenantId(), version);
        }
        KnowledgeReviewRecordBO review = new KnowledgeReviewRecordBO();
        review.setDocumentId(documentId);
        review.setVersionId(document.getCurrentVersionId());
        review.setAction(action);
        review.setCommentText(comment);
        review.setStatus(1);
        review.setDelFlag(0);
        enterpriseRepository.insertReview(actor.tenantId(), review);
        auditService.record(actor, document.getSpaceId(), "DOCUMENT", documentId, action, comment);
        if ("PUBLISHED".equals(target) || "ARCHIVED".equals(target)) {
            scheduleIndexRebuild(actor.tenantId(), document.getSpaceId());
        }
        return toView(document);
    }

    private void scheduleIndexRebuild(com.shiyu.ai.kernel.context.TenantId tenantId, Long spaceId) {
        TransactionHookExecutor.register(
                new com.shiyu.ai.common.core.tx.TransactionHook() {
                    /**
                     * {@code afterCommit} 执行当前类型定义的业务操作。
                     */
                    @Override
                    public void afterCommit() {
                        indexService.rebuild(tenantId, spaceId);
                    }
                });
    }

    private KnowledgeDocumentBO requireDocument(ActorContext actor, Long documentId) {
        requireActor(actor);
        KnowledgeDocumentBO document = documentRepository.selectById(actor.tenantId(), documentId);
        if (document == null
                || document.getTenantId() == null
                || !document.getTenantId().equals(actor.tenantId().value())) {
            throw new ServiceException("文档不存在: " + documentId);
        }
        return document;
    }

    private void requireActor(ActorContext actor) {
        if (actor == null) {
            throw new ServiceException("当前租户或用户上下文不存在");
        }
    }

    private void requireWrite(int rows, String operation) {
        if (rows < 1) {
            throw new ServiceException(operation + "失败: 未写入当前租户数据");
        }
    }

    private KnowledgeDocumentVersionBO copyVersion(KnowledgeDocumentVersionBO source) {
        KnowledgeDocumentVersionBO target = new KnowledgeDocumentVersionBO();
        target.setDocumentId(source.getDocumentId());
        target.setSpaceId(source.getSpaceId());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setStorageProvider(source.getStorageProvider());
        target.setObjectKey(source.getObjectKey());
        target.setMimeType(source.getMimeType());
        target.setFileSize(source.getFileSize());
        target.setChecksum(source.getChecksum());
        target.setParseStatus(source.getParseStatus());
        target.setModelProfile(source.getModelProfile());
        target.setDelFlag(0);
        return target;
    }

    private DocumentView toView(KnowledgeDocumentBO document) {
        return new DocumentView(
                document.getId(),
                document.getSpaceId(),
                document.getCurrentVersionId(),
                document.getTitle(),
                document.getDocType(),
                document.getSource(),
                document.getLifecycleStatus(),
                document.getParseStatus(),
                document.getObjectKey(),
                document.getMimeType(),
                document.getFileSize(),
                document.getChecksum(),
                document.getCreateTime(),
                document.getUpdateTime());
    }

    private VersionView toVersionView(KnowledgeDocumentVersionBO version) {
        return new VersionView(
                version.getId(),
                version.getDocumentId(),
                version.getSpaceId(),
                version.getVersionNo(),
                version.getTitle(),
                version.getLifecycleStatus(),
                version.getParseStatus(),
                version.getObjectKey(),
                version.getMimeType(),
                version.getFileSize(),
                version.getChecksum(),
                version.getModelProfile(),
                version.getPublishedAt(),
                version.getCreateTime());
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "txt" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

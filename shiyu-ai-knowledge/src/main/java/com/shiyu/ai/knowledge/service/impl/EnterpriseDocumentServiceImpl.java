package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.core.tx.TransactionTemplateExecutor;
import com.shiyu.ai.common.core.tx.TransactionHookExecutor;
import com.shiyu.ai.common.storage.StorageMetadataStore;
import com.shiyu.ai.common.storage.ObjectStorage;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDocumentVersionBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeIngestionJobBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeReviewRecordBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeSpaceBO;
import com.shiyu.ai.knowledge.port.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.knowledge.port.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import com.shiyu.ai.knowledge.service.KnowledgeDocumentRelationService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDocumentServiceImpl implements EnterpriseDocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    private final KnowledgeSpaceService spaceService;
    private final KnowledgeAuditService auditService;
    private final KnowledgeDocumentRelationService documentRelationService;
    private final DocumentIngestionService ingestionService;
    private final KnowledgeIndexService indexService;
    private final TransactionTemplateExecutor transactionTemplateExecutor;
    private final StorageMetadataStore storageMetadataStore;
    private final ObjectStorage objectStorage;

    @Override
    public PageData<DocumentView> page(Long spaceId, int pageNum, int pageSize,
                                       String keyword, String lifecycleStatus, String parseStatus) {
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER);
        PageData<KnowledgeDocumentBO> result = documentRepository.pageBySpace(
                spaceId, pageNum, pageSize, keyword, lifecycleStatus, parseStatus);
        return new PageData<>(result.getItems().stream().map(this::toView).toList(),
                result.getTotal());
    }

    @Override
    public DocumentView get(Long documentId) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
        return toView(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult registerStoredFile(StoredFileRequest request) {
        spaceService.requireAccess(request.spaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        KnowledgeDocumentBO duplicate = documentRepository.findBySpaceAndChecksum(
                request.spaceId(), request.checksum());
        if (duplicate != null) {
            return new UploadResult(toView(duplicate), duplicate.getCurrentVersionId(), null, true);
        }

        KnowledgeDocumentBO document = new KnowledgeDocumentBO();
        document.setSpaceId(request.spaceId());
        document.setTitle(request.title());
        // The source text is populated by the ingestion worker. H2 keeps this
        // legacy column non-null for backwards compatibility, so use an empty
        // value until parsing completes instead of inserting SQL NULL.
        document.setContent("");
        document.setDocType(extension(request.originalName()));
        document.setSource("UPLOAD");
        document.setLifecycleStatus("DRAFT");
        document.setParseStatus("PENDING");
        document.setStorageProvider(defaultText(request.storageProvider(), "local"));
        Long tenantId = LoginContextHolder.getCurrentTenantId();
        Long storageObjectId = tenantId == null ? null
                : storageMetadataStore.findObjectByKey(tenantId, request.objectKey())
                .map(StorageMetadataStore.StorageObjectRecord::id).orElse(null);
        document.setStorageObjectId(storageObjectId);
        document.setObjectKey(request.objectKey());
        document.setMimeType(request.mimeType());
        document.setFileSize(request.fileSize());
        document.setChecksum(request.checksum());
        document.setStatus(1);
        document.setDelFlag(0);
        documentRepository.insert(document);

        KnowledgeSpaceBO space = enterpriseRepository.findSpace(request.spaceId());
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
        enterpriseRepository.insertVersion(version);

        document.setCurrentVersionId(version.getId());
        documentRepository.update(document);

        String jobKey = "INGEST:" + request.spaceId() + ":" + request.checksum();
        KnowledgeIngestionJobBO job = enterpriseRepository.findJobByKey(jobKey);
        if (job == null) {
            job = new KnowledgeIngestionJobBO();
            job.setJobKey(jobKey);
            job.setJobType("INGEST");
            job.setSpaceId(request.spaceId());
            job.setDocumentId(document.getId());
            job.setVersionId(version.getId());
            job.setJobStatus("PENDING");
            job.setStage("QUEUED");
            job.setProgress(0);
            job.setAttempts(0);
            job.setMaxAttempts(3);
            job.setLockVersion(0L);
            job.setDelFlag(0);
            enterpriseRepository.insertJob(job);
        } else {
            // A previous document with the same checksum may have been
            // deleted. Rebind its idempotency record so a re-upload can be
            // ingested again without violating the unique job key.
            KnowledgeDocumentBO previous = job.getDocumentId() == null
                    ? null : documentRepository.selectById(job.getDocumentId());
            if (previous == null || Integer.valueOf(1).equals(previous.getDelFlag())) {
                job.setDocumentId(document.getId());
                job.setVersionId(version.getId());
                job.setJobStatus("PENDING");
                job.setStage("QUEUED");
                job.setProgress(0);
                job.setAttempts(0);
                job.setErrorMessage(null);
                job.setHeartbeatTime(null);
                job.setStartedTime(null);
                job.setFinishedTime(null);
                job.setDelFlag(0);
                enterpriseRepository.updateJob(job);
            }
        }
        auditService.record(request.spaceId(), "DOCUMENT", document.getId(), "UPLOAD", request);
        return new UploadResult(toView(document), version.getId(), job.getId(), false);
    }

    @Override
    public List<VersionView> versions(Long documentId) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.VIEWER);
        return enterpriseRepository.findVersions(documentId).stream().map(this::toVersionView).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView submit(Long documentId, String comment) {
        return transition(documentId, "DRAFT", "REVIEWING", "SUBMIT",
                KnowledgeSpaceService.SpaceRole.EDITOR, comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView approve(Long documentId, String comment) {
        return transition(documentId, "REVIEWING", "PUBLISHED", "APPROVE",
                KnowledgeSpaceService.SpaceRole.REVIEWER, comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView reject(Long documentId, String comment) {
        return transition(documentId, "REVIEWING", "DRAFT", "REJECT",
                KnowledgeSpaceService.SpaceRole.REVIEWER, comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView publish(Long documentId, String comment) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        KnowledgeSpaceBO space = enterpriseRepository.findSpace(document.getSpaceId());
        if (space != null && "REQUIRED".equals(space.getBindingMode())
                && documentRelationService.listPointIds(documentId).isEmpty()) {
            throw new ServiceException("当前知识空间要求文档至少关联一个知识点后才能发布");
        }
        KnowledgeSpaceService.SpaceRole required =
                space != null && "REQUIRED".equals(space.getReviewMode())
                        ? KnowledgeSpaceService.SpaceRole.REVIEWER
                        : KnowledgeSpaceService.SpaceRole.EDITOR;
        return transition(documentId, null, "PUBLISHED", "PUBLISH", required, comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView archive(Long documentId, String comment) {
        return transition(documentId, "PUBLISHED", "ARCHIVED", "ARCHIVE",
                KnowledgeSpaceService.SpaceRole.EDITOR, comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView rollback(Long documentId, Long versionId) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        boolean wasPublished = "PUBLISHED".equals(document.getLifecycleStatus());
        KnowledgeDocumentVersionBO source = enterpriseRepository.findVersion(versionId);
        if (source == null || !documentId.equals(source.getDocumentId())) {
            throw new ServiceException("文档版本不存在");
        }
        KnowledgeDocumentVersionBO version = copyVersion(source);
        version.setVersionNo(enterpriseRepository.nextVersionNo(documentId));
        version.setLifecycleStatus("DRAFT");
        version.setPublishedAt(null);
        enterpriseRepository.insertVersion(version);
        document.setCurrentVersionId(version.getId());
        document.setTitle(version.getTitle());
        document.setLifecycleStatus("DRAFT");
        document.setParseStatus(version.getParseStatus());
        document.setObjectKey(version.getObjectKey());
        document.setChecksum(version.getChecksum());
        documentRepository.update(document);
        auditService.record(document.getSpaceId(), "DOCUMENT", documentId, "ROLLBACK", versionId);
        if (wasPublished) {
            scheduleIndexRebuild(document.getSpaceId());
        }
        return toView(document);
    }

    @Override
    public void delete(Long documentId) {
        DeletionContext deletion = transactionTemplateExecutor.execute(new DefaultTransactionDefinition(), () -> {
            KnowledgeDocumentBO document = requireDocument(documentId);
            spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
            documentRelationService.removeDocumentRelations(documentId);
            ingestionService.delete(documentId);
            documentRepository.deleteById(documentId);
            auditService.record(document.getSpaceId(), "DOCUMENT", documentId, "DELETE", null);
            return new DeletionContext(LoginContextHolder.getCurrentTenantId(), document.getSpaceId(),
                    document.getObjectKey());
        });
        if (deletion.objectKey() != null && !deletion.objectKey().isBlank()) {
            boolean physicalDeleted = false;
            try {
                objectStorage.delete(deletion.objectKey());
                physicalDeleted = true;
            } catch (IOException exception) {
                log.warn("文档记录已删除，但物理对象删除失败，等待存储一致性任务处理，objectKey={}",
                        deletion.objectKey(), exception);
            }
            if (physicalDeleted && deletion.tenantId() != null) {
                storageMetadataStore.markObjectDeleted(deletion.tenantId(), deletion.objectKey());
            }
        }
        // The database transaction must commit before a new physical index is activated.
        // Otherwise a later rollback could leave the active index pointing at a deleted
        // document or leave a new index version active without the corresponding rows.
        if (deletion.tenantId() != null && deletion.spaceId() != null) {
            try {
                indexService.rebuild(deletion.tenantId(), deletion.spaceId());
            } catch (RuntimeException exception) {
                log.error("Document deleted but space index rebuild failed, documentId={}, spaceId={}",
                        documentId, deletion.spaceId(), exception);
                throw new ServiceException("文档已删除，但空间索引重建失败，请稍后在索引任务中重试");
            }
        }
    }

    private record DeletionContext(Long tenantId, Long spaceId, String objectKey) {
    }

    protected DocumentView transition(Long documentId, String expected, String target,
                                      String action, KnowledgeSpaceService.SpaceRole role,
                                      String comment) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        spaceService.requireAccess(document.getSpaceId(), role);
        if (expected != null && !expected.equals(document.getLifecycleStatus())) {
            throw new ServiceException("当前状态不允许执行该操作: " + document.getLifecycleStatus());
        }
        if ("PUBLISHED".equals(target) && !"READY".equals(document.getParseStatus())) {
            throw new ServiceException("文档尚未解析完成，不能发布");
        }
        document.setLifecycleStatus(target);
        documentRepository.update(document);
        KnowledgeDocumentVersionBO version = enterpriseRepository.findVersion(document.getCurrentVersionId());
        if (version != null) {
            version.setLifecycleStatus(target);
            if ("PUBLISHED".equals(target)) {
                version.setPublishedAt(LocalDateTime.now());
            }
            enterpriseRepository.updateVersion(version);
        }
        KnowledgeReviewRecordBO review = new KnowledgeReviewRecordBO();
        review.setDocumentId(documentId);
        review.setVersionId(document.getCurrentVersionId());
        review.setAction(action);
        review.setCommentText(comment);
        review.setStatus(1);
        review.setDelFlag(0);
        enterpriseRepository.insertReview(review);
        auditService.record(document.getSpaceId(), "DOCUMENT", documentId, action, comment);
        if ("PUBLISHED".equals(target) || "ARCHIVED".equals(target)) {
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            if (tenantId == null) {
                throw new ServiceException("当前租户上下文不存在");
            }
            scheduleIndexRebuild(document.getSpaceId());
        }
        return toView(document);
    }

    private void scheduleIndexRebuild(Long spaceId) {
        Long tenantId = LoginContextHolder.getCurrentTenantId();
        if (tenantId == null) {
            throw new ServiceException("褰撳墠绉熸埛涓婁笅鏂囦笉瀛樺湪");
        }
        TransactionHookExecutor.register(new com.shiyu.ai.common.core.tx.TransactionHook() {
            @Override
            public void afterCommit() {
                indexService.rebuild(tenantId, spaceId);
            }
        });
    }

    private KnowledgeDocumentBO requireDocument(Long documentId) {
        KnowledgeDocumentBO document = documentRepository.selectById(documentId);
        if (document == null) {
            throw new ServiceException("文档不存在: " + documentId);
        }
        return document;
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
        return new DocumentView(document.getId(), document.getSpaceId(),
                document.getCurrentVersionId(), document.getTitle(), document.getDocType(),
                document.getSource(), document.getLifecycleStatus(), document.getParseStatus(),
                document.getObjectKey(), document.getMimeType(), document.getFileSize(),
                document.getChecksum(), document.getCreateTime(), document.getUpdateTime());
    }

    private VersionView toVersionView(KnowledgeDocumentVersionBO version) {
        return new VersionView(version.getId(), version.getDocumentId(), version.getSpaceId(),
                version.getVersionNo(), version.getTitle(), version.getLifecycleStatus(),
                version.getParseStatus(), version.getObjectKey(), version.getMimeType(),
                version.getFileSize(), version.getChecksum(), version.getModelProfile(),
                version.getPublishedAt(), version.getCreateTime());
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "txt" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}

package com.shiyu.ai.knowledge.service.impl;

import com.shiyu.ai.common.core.api.PageData;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.dal.knowledge.bo.KnowledgeDocumentBO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDocumentVersionDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeIngestionJobDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeReviewRecordDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeSpaceDO;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeDocumentRepository;
import com.shiyu.ai.dal.knowledge.repository.KnowledgeEnterpriseRepository;
import com.shiyu.ai.knowledge.rag.DocumentIngestionService;
import com.shiyu.ai.knowledge.index.KnowledgeIndexService;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeAuditService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EnterpriseDocumentServiceImpl implements EnterpriseDocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeEnterpriseRepository enterpriseRepository;
    private final KnowledgeSpaceService spaceService;
    private final KnowledgeAuditService auditService;
    private final DocumentIngestionService ingestionService;
    private final KnowledgeIndexService indexService;

    @Override
    public PageData<DocumentView> page(Long spaceId, int pageNum, int pageSize,
                                       String keyword, String lifecycleStatus) {
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.VIEWER);
        PageData<KnowledgeDocumentBO> result = documentRepository.pageBySpace(
                spaceId, pageNum, pageSize, keyword, lifecycleStatus);
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
        document.setDocType(extension(request.originalName()));
        document.setSource("UPLOAD");
        document.setLifecycleStatus("DRAFT");
        document.setParseStatus("PENDING");
        document.setStorageProvider(defaultText(request.storageProvider(), "local"));
        document.setObjectKey(request.objectKey());
        document.setMimeType(request.mimeType());
        document.setFileSize(request.fileSize());
        document.setChecksum(request.checksum());
        document.setStatus(1);
        documentRepository.insert(document);

        KnowledgeSpaceDO space = enterpriseRepository.findSpace(request.spaceId());
        KnowledgeDocumentVersionDO version = new KnowledgeDocumentVersionDO();
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
        enterpriseRepository.insertVersion(version);

        document.setCurrentVersionId(version.getId());
        documentRepository.update(document);

        String jobKey = "INGEST:" + request.spaceId() + ":" + request.checksum();
        KnowledgeIngestionJobDO job = enterpriseRepository.findJobByKey(jobKey);
        if (job == null) {
            job = new KnowledgeIngestionJobDO();
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
            enterpriseRepository.insertJob(job);
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
        KnowledgeSpaceDO space = enterpriseRepository.findSpace(document.getSpaceId());
        KnowledgeSpaceService.SpaceRole required =
                space != null && "REQUIRED".equals(space.getReviewMode())
                        ? KnowledgeSpaceService.SpaceRole.REVIEWER
                        : KnowledgeSpaceService.SpaceRole.EDITOR;
        return transition(documentId, null, "PUBLISHED", "PUBLISH", required, comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentView rollback(Long documentId, Long versionId) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        KnowledgeDocumentVersionDO source = enterpriseRepository.findVersion(versionId);
        if (source == null || !documentId.equals(source.getDocumentId())) {
            throw new ServiceException("文档版本不存在");
        }
        KnowledgeDocumentVersionDO version = copyVersion(source);
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
        return toView(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long documentId) {
        KnowledgeDocumentBO document = requireDocument(documentId);
        spaceService.requireAccess(document.getSpaceId(), KnowledgeSpaceService.SpaceRole.EDITOR);
        ingestionService.delete(documentId);
        documentRepository.deleteById(documentId);
        auditService.record(document.getSpaceId(), "DOCUMENT", documentId, "DELETE", null);
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
        KnowledgeDocumentVersionDO version = enterpriseRepository.findVersion(document.getCurrentVersionId());
        if (version != null) {
            version.setLifecycleStatus(target);
            if ("PUBLISHED".equals(target)) {
                version.setPublishedAt(LocalDateTime.now());
            }
            enterpriseRepository.updateVersion(version);
        }
        KnowledgeReviewRecordDO review = new KnowledgeReviewRecordDO();
        review.setDocumentId(documentId);
        review.setVersionId(document.getCurrentVersionId());
        review.setAction(action);
        review.setCommentText(comment);
        enterpriseRepository.insertReview(review);
        auditService.record(document.getSpaceId(), "DOCUMENT", documentId, action, comment);
        if ("PUBLISHED".equals(target)) {
            Long tenantId = LoginContextHolder.getCurrentTenantId();
            if (tenantId == null) {
                throw new ServiceException("当前租户上下文不存在");
            }
            indexService.rebuild(tenantId, document.getSpaceId());
        }
        return toView(document);
    }

    private KnowledgeDocumentBO requireDocument(Long documentId) {
        KnowledgeDocumentBO document = documentRepository.selectById(documentId);
        if (document == null) {
            throw new ServiceException("文档不存在: " + documentId);
        }
        return document;
    }

    private KnowledgeDocumentVersionDO copyVersion(KnowledgeDocumentVersionDO source) {
        KnowledgeDocumentVersionDO target = new KnowledgeDocumentVersionDO();
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
        return target;
    }

    private DocumentView toView(KnowledgeDocumentBO document) {
        return new DocumentView(document.getId(), document.getSpaceId(),
                document.getCurrentVersionId(), document.getTitle(), document.getDocType(),
                document.getSource(), document.getLifecycleStatus(), document.getParseStatus(),
                document.getObjectKey(), document.getMimeType(), document.getFileSize(),
                document.getChecksum(), document.getCreateTime(), document.getUpdateTime());
    }

    private VersionView toVersionView(KnowledgeDocumentVersionDO version) {
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

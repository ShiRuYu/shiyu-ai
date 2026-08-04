package com.shiyu.ai.knowledge.service;

import com.shiyu.ai.common.core.api.PageData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public interface EnterpriseDocumentService {

    PageData<DocumentView> page(Long spaceId, int pageNum, int pageSize,
                                String keyword, String lifecycleStatus, String parseStatus);

    DocumentView get(Long documentId);

    UploadResult registerStoredFile(StoredFileRequest request);

    List<VersionView> versions(Long documentId);

    DocumentView submit(Long documentId, String comment);

    DocumentView approve(Long documentId, String comment);

    DocumentView reject(Long documentId, String comment);

    DocumentView publish(Long documentId, String comment);

    DocumentView archive(Long documentId, String comment);

    DocumentView rollback(Long documentId, Long versionId);

    void delete(Long documentId);

    record StoredFileRequest(@NotNull Long spaceId, @NotBlank String title,
                             @NotBlank String originalName, @NotBlank String objectKey,
                             String storageProvider, String mimeType, long fileSize,
                             @NotBlank String checksum) {
    }

    record UploadResult(DocumentView document, Long versionId, Long jobId,
                        boolean duplicate) {
    }

    record DocumentView(Long id, Long spaceId, Long currentVersionId, String title,
                        String docType, String source, String lifecycleStatus,
                        String parseStatus, String objectKey, String mimeType,
                        Long fileSize, String checksum, LocalDateTime createTime,
                        LocalDateTime updateTime) {
    }

    record VersionView(Long id, Long documentId, Long spaceId, Integer versionNo,
                       String title, String lifecycleStatus, String parseStatus,
                       String objectKey, String mimeType, Long fileSize, String checksum,
                       String modelProfile, LocalDateTime publishedAt,
                       LocalDateTime createTime) {
    }
}

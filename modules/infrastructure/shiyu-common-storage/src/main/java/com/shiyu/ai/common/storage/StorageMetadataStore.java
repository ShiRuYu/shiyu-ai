package com.shiyu.ai.common.storage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Persistent metadata boundary for storage objects and resumable uploads.
 * The physical object stays in the configured storage provider; this contract
 * keeps the database as the source of truth after an application restart.
 */
public interface StorageMetadataStore {

    long createObject(CreateObject command);

    void markObjectAvailable(long objectId, String objectKey, String provider,
                             long size, String contentType, String checksum);

    void markObjectFailed(long objectId, String message);

    void markObjectDeleted(long tenantId, String objectKey);

    Optional<StorageObjectRecord> findObjectByKey(long tenantId, String objectKey);

    List<StorageObjectRecord> listObjects(long tenantId, String namespace, int offset, int limit);

    long createUploadSession(CreateUploadSession command);

    Optional<UploadSessionRecord> findUploadSession(long tenantId, String sessionId);

    /** Returns sessions whose resumable-upload lease has expired. */
    default List<UploadSessionRecord> findExpiredUploadSessions(Instant now) {
        return List.of();
    }

    void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum);

    List<Integer> uploadedChunks(String sessionId);

    void updateUploadSessionStatus(String sessionId, String status, String errorMessage);

    void deleteUploadSession(String sessionId);

    default boolean persistent() {
        return true;
    }

    record CreateObject(long tenantId, Long spaceId, String namespace, String originalName,
                        String objectKey, String provider, String contentType,
                        long size, String checksum, String status) {
    }

    record CreateUploadSession(String sessionId, long tenantId, Long spaceId,
                               String namespace, String fileName, String contentType,
                               long expectedSize, String expectedChecksum, int totalChunks,
                               String tempPath, Instant expiresAt) {
    }

    record StorageObjectRecord(long id, long tenantId, Long spaceId, String namespace,
                               String objectKey, String provider, String originalName,
                               String contentType, long size, String checksum, String status,
                               Instant createTime, Instant updateTime) {
    }

    record UploadSessionRecord(String sessionId, long tenantId, Long spaceId,
                               String namespace, String fileName, String contentType,
                               long expectedSize, String expectedChecksum, int totalChunks,
                               String status, String tempPath, Instant expiresAt) {
    }
}

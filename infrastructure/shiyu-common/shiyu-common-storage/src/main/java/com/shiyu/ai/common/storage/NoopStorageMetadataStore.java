package com.shiyu.ai.common.storage;

import java.util.List;
import java.util.Optional;

/** Test-only fallback used when the storage module is instantiated without a DataSource. */
final class NoopStorageMetadataStore implements StorageMetadataStore {
    static final NoopStorageMetadataStore INSTANCE = new NoopStorageMetadataStore();

    private NoopStorageMetadataStore() {
    }

    @Override public long createObject(CreateObject command) { return 0L; }
    @Override public void markObjectAvailable(long objectId, String objectKey, String provider,
                                               long size, String contentType, String checksum) { }
    @Override public void markObjectFailed(long objectId, String message) { }
    @Override public void markObjectDeleted(long tenantId, String objectKey) { }
    @Override public Optional<StorageObjectRecord> findObjectByKey(long tenantId, String objectKey) {
        return Optional.empty();
    }
    @Override public List<StorageObjectRecord> listObjects(long tenantId, String namespace,
                                                             int offset, int limit) {
        return List.of();
    }
    @Override public long createUploadSession(CreateUploadSession command) { return 0L; }
    @Override public Optional<UploadSessionRecord> findUploadSession(long tenantId, String sessionId) {
        return Optional.empty();
    }
    @Override public void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum) { }
    @Override public List<Integer> uploadedChunks(String sessionId) { return List.of(); }
    @Override public void updateUploadSessionStatus(String sessionId, String status, String errorMessage) { }
    @Override public void deleteUploadSession(String sessionId) { }
    @Override public boolean persistent() { return false; }
}

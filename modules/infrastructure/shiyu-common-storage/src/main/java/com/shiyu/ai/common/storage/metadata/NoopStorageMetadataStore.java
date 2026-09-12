package com.shiyu.ai.common.storage.metadata;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.util.List;
import java.util.Optional;

/**
 * 提供不落库的存储元数据适配器，用于无元数据场景。
 */
public final class NoopStorageMetadataStore implements StorageMetadataStore {
    public static final NoopStorageMetadataStore INSTANCE = new NoopStorageMetadataStore();

    private NoopStorageMetadataStore() {}

    /**
     * {@code createObject} 写入或更新当前模块中的业务数据。
     *
     * @param command 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long createObject(CreateObject command) {
        return 0L;
    }

    /**
     * {@code markObjectAvailable} 执行当前类型定义的业务操作。
     *
     * @param objectId 参数值，用于执行当前操作。
     * @param objectKey 参数值，用于执行当前操作。
     * @param provider 参数值，用于执行当前操作。
     * @param size 参数值，用于执行当前操作。
     * @param contentType 参数值，用于执行当前操作。
     * @param checksum 参数值，用于执行当前操作。
     */
    @Override
    public void markObjectAvailable(
            long objectId,
            String objectKey,
            String provider,
            long size,
            String contentType,
            String checksum) {}

    /**
     * {@code markObjectFailed} 执行当前类型定义的业务操作。
     *
     * @param objectId 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     */
    @Override
    public void markObjectFailed(long objectId, String message) {}

    /**
     * {@code markObjectDeleted} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param objectKey 参数值，用于执行当前操作。
     */
    @Override
    public void markObjectDeleted(long tenantId, String objectKey) {}

    /**
     * {@code findObjectByKey} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param objectKey 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<StorageObjectRecord> findObjectByKey(long tenantId, String objectKey) {
        return Optional.empty();
    }

    /**
     * {@code listObjects} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param namespace 参数值，用于执行当前操作。
     * @param offset 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<StorageObjectRecord> listObjects(
            long tenantId, String namespace, int offset, int limit) {
        return List.of();
    }

    /**
     * {@code createUploadSession} 写入或更新当前模块中的业务数据。
     *
     * @param command 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long createUploadSession(CreateUploadSession command) {
        return 0L;
    }

    /**
     * {@code findUploadSession} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param sessionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<UploadSessionRecord> findUploadSession(long tenantId, String sessionId) {
        return Optional.empty();
    }

    /**
     * {@code markChunkUploaded} 执行当前类型定义的业务操作。
     *
     * @param sessionId 参数值，用于执行当前操作。
     * @param chunkIndex 参数值，用于执行当前操作。
     * @param size 参数值，用于执行当前操作。
     * @param checksum 参数值，用于执行当前操作。
     */
    @Override
    public void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum) {}

    /**
     * {@code uploadedChunks} 执行当前类型定义的业务操作。
     *
     * @param sessionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Integer> uploadedChunks(String sessionId) {
        return List.of();
    }

    /**
     * {@code updateUploadSessionStatus} 写入或更新当前模块中的业务数据。
     *
     * @param sessionId 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     * @param errorMessage 参数值，用于执行当前操作。
     */
    @Override
    public void updateUploadSessionStatus(String sessionId, String status, String errorMessage) {}

    /**
     * {@code deleteUploadSession} 释放或移除当前操作涉及的资源。
     *
     * @param sessionId 参数值，用于执行当前操作。
     */
    @Override
    public void deleteUploadSession(String sessionId) {}

    /**
     * {@code persistent} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean persistent() {
        return false;
    }
}

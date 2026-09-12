package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * StorageMetadataStore 接口，定义基础设施模块的能力边界。
 */
public interface StorageMetadataStore {

    /**
     * 创建并保存业务对象。
     *
     * @param command 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    long createObject(CreateObject command);

    /**
     * 执行 {@code markObjectAvailable} 定义的接口操作。
     *
     * @param objectId 方法参数。
     * @param objectKey 方法参数。
     * @param provider 方法参数。
     * @param size 方法参数。
     * @param contentType 方法参数。
     * @param checksum 方法参数。
     */
    void markObjectAvailable(
            long objectId,
            String objectKey,
            String provider,
            long size,
            String contentType,
            String checksum);

    /**
     * 执行 {@code markObjectFailed} 定义的接口操作。
     *
     * @param objectId 方法参数。
     * @param message 方法参数。
     */
    void markObjectFailed(long objectId, String message);

    /**
     * 执行 {@code markObjectDeleted} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param objectKey 方法参数。
     */
    void markObjectDeleted(long tenantId, String objectKey);

    /**
     * 更新object提供者。
     *
     * @param tenantId 租户标识。
     * @param objectKey objectKey 参数。
     * @param provider provider 参数。
     */
    default void updateObjectProvider(long tenantId, String objectKey, String provider) {}

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param objectKey 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<StorageObjectRecord> findObjectByKey(long tenantId, String objectKey);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     * @param offset 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<StorageObjectRecord> listObjects(long tenantId, String namespace, int offset, int limit);

    /**
     * 创建并保存业务对象。
     *
     * @param command 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    long createUploadSession(CreateUploadSession command);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param sessionId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<UploadSessionRecord> findUploadSession(long tenantId, String sessionId);

    /**
     * 查询expireduploadsessions。
     *
     * @param now now 参数。
     *
     * @return 结果列表。
     */
    default List<UploadSessionRecord> findExpiredUploadSessions(Instant now) {
        return List.of();
    }

    /**
     * 执行 {@code markChunkUploaded} 定义的接口操作。
     *
     * @param sessionId 方法参数。
     * @param chunkIndex 方法参数。
     * @param size 方法参数。
     * @param checksum 方法参数。
     */
    void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum);

    /**
     * 执行 {@code uploadedChunks} 定义的接口操作。
     *
     * @param sessionId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Integer> uploadedChunks(String sessionId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param sessionId 方法参数。
     * @param status 对象状态。
     * @param errorMessage 方法参数。
     */
    void updateUploadSessionStatus(String sessionId, String status, String errorMessage);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param sessionId 方法参数。
     */
    void deleteUploadSession(String sessionId);

    /**
     * 执行 {@code persistent} 定义的接口操作。
     *
     * @return 条件是否满足。
     */
    default boolean persistent() {
        return true;
    }

    /**
     * {@code CreateObject} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param namespace 命名空间，表示该记录组件承载的数据。
     * @param originalName originalName 属性，表示该记录组件承载的数据。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param provider 提供方，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param checksum checksum 属性，表示该记录组件承载的数据。
     * @param status 状态，表示该记录组件承载的数据。
     */
    record CreateObject(
            long tenantId,
            Long spaceId,
            String namespace,
            String originalName,
            String objectKey,
            String provider,
            String contentType,
            long size,
            String checksum,
            String status) {}

    /**
     * {@code CreateUploadSession} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param sessionId sessionId 属性，表示该记录组件承载的数据。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param namespace 命名空间，表示该记录组件承载的数据。
     * @param fileName 文件名，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param expectedSize expectedSize 属性，表示该记录组件承载的数据。
     * @param expectedChecksum expectedChecksum 属性，表示该记录组件承载的数据。
     * @param totalChunks totalChunks 属性，表示该记录组件承载的数据。
     * @param tempPath tempPath 属性，表示该记录组件承载的数据。
     * @param expiresAt 过期时间，表示该记录组件承载的数据。
     */
    record CreateUploadSession(
            String sessionId,
            long tenantId,
            Long spaceId,
            String namespace,
            String fileName,
            String contentType,
            long expectedSize,
            String expectedChecksum,
            int totalChunks,
            String tempPath,
            Instant expiresAt) {}

    /**
     * {@code StorageObjectRecord} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param namespace 命名空间，表示该记录组件承载的数据。
     * @param objectKey objectKey 属性，表示该记录组件承载的数据。
     * @param provider 提供方，表示该记录组件承载的数据。
     * @param originalName originalName 属性，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param checksum checksum 属性，表示该记录组件承载的数据。
     * @param status 状态，表示该记录组件承载的数据。
     * @param createTime createTime 属性，表示该记录组件承载的数据。
     * @param updateTime updateTime 属性，表示该记录组件承载的数据。
     */
    record StorageObjectRecord(
            long id,
            long tenantId,
            Long spaceId,
            String namespace,
            String objectKey,
            String provider,
            String originalName,
            String contentType,
            long size,
            String checksum,
            String status,
            Instant createTime,
            Instant updateTime) {}

    /**
     * {@code UploadSessionRecord} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param sessionId sessionId 属性，表示该记录组件承载的数据。
     * @param tenantId 租户标识，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param namespace 命名空间，表示该记录组件承载的数据。
     * @param fileName 文件名，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param expectedSize expectedSize 属性，表示该记录组件承载的数据。
     * @param expectedChecksum expectedChecksum 属性，表示该记录组件承载的数据。
     * @param totalChunks totalChunks 属性，表示该记录组件承载的数据。
     * @param status 状态，表示该记录组件承载的数据。
     * @param tempPath tempPath 属性，表示该记录组件承载的数据。
     * @param expiresAt 过期时间，表示该记录组件承载的数据。
     */
    record UploadSessionRecord(
            String sessionId,
            long tenantId,
            Long spaceId,
            String namespace,
            String fileName,
            String contentType,
            long expectedSize,
            String expectedChecksum,
            int totalChunks,
            String status,
            String tempPath,
            Instant expiresAt) {}
}

package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 管理 Storage Metadata 相关的运行时状态、注册信息或临时数据。
 */
public interface StorageMetadataStore {

    /**
     * 创建或保存 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param command 本次流程携带的事件或业务数据。
     * @return 返回 Storage Metadata 相关操作生成的结果数据。
     */
    long createObject(CreateObject command);

    /**
     * 执行 Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param objectId 用于定位object的标识。
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param size 每页返回的数据数量。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param checksum 用于完成本次业务处理的 checksum 参数。
     */
    void markObjectAvailable(
            long objectId,
            String objectKey,
            String provider,
            long size,
            String contentType,
            String checksum);

    /**
     * 执行 Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param objectId 用于定位object的标识。
     * @param message 本次流程携带的事件或业务数据。
     */
    void markObjectFailed(long objectId, String message);

    /**
     * 执行 Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
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
     * 查询 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<StorageObjectRecord> findObjectByKey(long tenantId, String objectKey);

    /**
     * 查询 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<StorageObjectRecord> listObjects(long tenantId, String namespace, int offset, int limit);

    /**
     * 创建或保存 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param command 本次流程携带的事件或业务数据。
     * @return 返回 Storage Metadata 相关操作生成的结果数据。
     */
    long createUploadSession(CreateUploadSession command);

    /**
     * 查询 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sessionId 用于定位session的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<UploadSessionRecord> findUploadSession(long tenantId, String sessionId);

    /**
     * 查询 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param now 用于完成本次业务处理的 now 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    default List<UploadSessionRecord> findExpiredUploadSessions(Instant now) {
        return List.of();
    }

    /**
     * 执行 Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     * @param chunkIndex 用于完成本次业务处理的 chunkIndex 参数。
     * @param size 每页返回的数据数量。
     * @param checksum 用于完成本次业务处理的 checksum 参数。
     */
    void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum);

    /**
     * 执行 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param sessionId 用于定位session的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Integer> uploadedChunks(String sessionId);

    /**
     * 更新或设置 Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @param errorMessage 用于完成本次业务处理的 errorMessage 参数。
     */
    void updateUploadSessionStatus(String sessionId, String status, String errorMessage);

    /**
     * 删除或移除 Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     */
    void deleteUploadSession(String sessionId);

    /**
     * 执行 Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @return 返回本次条件判断是否成立。
     */
    default boolean persistent() {
        return true;
    }

    /**
     * 封装 Create Object 相关的不可变数据及其字段约束。
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
     * 封装 Create Upload Session 相关的不可变数据及其字段约束。
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
     * 封装 Storage Object 相关的不可变数据及其字段约束。
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
     * 封装 Upload Session 相关的不可变数据及其字段约束。
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

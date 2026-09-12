package com.shiyu.ai.common.storage.metadata;

import com.shiyu.ai.common.core.jdbc.JdbcDialect;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 通过 JDBC 持久化文件对象的元数据和校验信息。
 */
@Repository
public class JdbcStorageMetadataStore implements StorageMetadataStore {

    /**
     * JDBC模板，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbcTemplate;
    /**
     * dialect 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final JdbcDialect dialect;

    /**
     * {@code JdbcStorageMetadataStore} 创建并初始化当前类型实例。
     *
     * @param jdbcTemplate 参数值，用于执行当前操作。
     */
    public JdbcStorageMetadataStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dialect = JdbcDialect.detect(jdbcTemplate);
    }

    /**
     * {@code createObject} 写入或更新当前模块中的业务数据。
     *
     * @param command 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public long createObject(CreateObject command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    "INSERT INTO storage_object (tenant_id, space_id, namespace,"
                                        + " object_key, storage_provider, original_name,"
                                        + " content_type, file_size, checksum, status, create_time,"
                                        + " update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
                                        + " CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                                    Statement.RETURN_GENERATED_KEYS);
                    statement.setLong(1, command.tenantId());
                    if (command.spaceId() == null) statement.setObject(2, null);
                    else statement.setLong(2, command.spaceId());
                    statement.setString(3, command.namespace());
                    statement.setString(4, command.objectKey());
                    statement.setString(5, command.provider());
                    statement.setString(6, command.originalName());
                    statement.setString(7, command.contentType());
                    statement.setLong(8, command.size());
                    statement.setString(9, command.checksum());
                    statement.setString(10, command.status());
                    return statement;
                },
                keyHolder);
        Number key = null;
        if (keyHolder.getKeys() != null) {
            Object id = keyHolder.getKeys().get("ID");
            if (id == null) id = keyHolder.getKeys().get("id");
            if (id instanceof Number number) key = number;
        }
        if (key == null) key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("storage_object ID was not generated");
        return key.longValue();
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
            String checksum) {
        jdbcTemplate.update(
                "UPDATE storage_object SET object_key=?, storage_provider=?, file_size=?,"
                        + " content_type=?, checksum=?, status='AVAILABLE',"
                        + " update_time=CURRENT_TIMESTAMP WHERE id=?",
                objectKey,
                provider,
                size,
                contentType,
                checksum,
                objectId);
    }

    /**
     * {@code markObjectFailed} 执行当前类型定义的业务操作。
     *
     * @param objectId 参数值，用于执行当前操作。
     * @param message 参数值，用于执行当前操作。
     */
    @Override
    public void markObjectFailed(long objectId, String message) {
        jdbcTemplate.update(
                "UPDATE storage_object SET status='FAILED', metadata_json=?,"
                        + " update_time=CURRENT_TIMESTAMP WHERE id=?",
                message,
                objectId);
    }

    /**
     * {@code markObjectDeleted} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param objectKey 参数值，用于执行当前操作。
     */
    @Override
    public void markObjectDeleted(long tenantId, String objectKey) {
        jdbcTemplate.update(
                "UPDATE storage_object SET status='DELETED', update_time=CURRENT_TIMESTAMP "
                        + "WHERE tenant_id=? AND object_key=? AND status <> 'DELETED'",
                tenantId,
                objectKey);
    }

    /**
     * {@code updateObjectProvider} 写入或更新当前模块中的业务数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param objectKey 参数值，用于执行当前操作。
     * @param provider 参数值，用于执行当前操作。
     */
    @Override
    public void updateObjectProvider(long tenantId, String objectKey, String provider) {
        jdbcTemplate.update(
                "UPDATE storage_object SET storage_provider=?, update_time=CURRENT_TIMESTAMP "
                        + "WHERE tenant_id=? AND object_key=? AND status='AVAILABLE'",
                provider,
                tenantId,
                objectKey);
    }

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
        List<StorageObjectRecord> rows =
                jdbcTemplate.query(
                        "SELECT id, tenant_id, space_id, namespace, object_key, storage_provider,"
                            + " original_name, content_type, file_size, checksum, status,"
                            + " create_time, update_time FROM storage_object WHERE tenant_id=? AND"
                            + " object_key=? AND status <> 'DELETED'",
                        this::mapObject,
                        tenantId,
                        objectKey);
        return rows.stream().findFirst();
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
        return jdbcTemplate.query(
                "SELECT id, tenant_id, space_id, namespace, object_key, storage_provider,"
                        + " original_name, content_type, file_size, checksum, status, create_time,"
                        + " update_time FROM storage_object WHERE tenant_id=? AND namespace=? AND"
                        + " status <> 'DELETED' ORDER BY create_time DESC, id ASC LIMIT ? OFFSET ?",
                this::mapObject,
                tenantId,
                namespace,
                limit,
                offset);
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
        jdbcTemplate.update(
                "INSERT INTO storage_upload_session (session_id, tenant_id, space_id, namespace,"
                    + " file_name, content_type, expected_size, expected_checksum, total_chunks,"
                    + " status, temp_path, expires_at, create_time, update_time) VALUES (?, ?, ?,"
                    + " ?, ?, ?, ?, ?, ?, 'UPLOADING', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                command.sessionId(),
                command.tenantId(),
                command.spaceId(),
                command.namespace(),
                command.fileName(),
                command.contentType(),
                command.expectedSize(),
                command.expectedChecksum(),
                command.totalChunks(),
                command.tempPath(),
                command.expiresAt() == null ? null : Timestamp.from(command.expiresAt()));
        return 1L;
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
        List<UploadSessionRecord> rows =
                jdbcTemplate.query(
                        "SELECT session_id, tenant_id, space_id, namespace, file_name,"
                                + " content_type, expected_size, expected_checksum, total_chunks,"
                                + " status, temp_path, expires_at FROM storage_upload_session WHERE"
                                + " tenant_id=? AND session_id=?",
                        (rs, rowNum) ->
                                new UploadSessionRecord(
                                        rs.getString("session_id"),
                                        rs.getLong("tenant_id"),
                                        rs.getObject("space_id", Long.class),
                                        rs.getString("namespace"),
                                        rs.getString("file_name"),
                                        rs.getString("content_type"),
                                        rs.getLong("expected_size"),
                                        rs.getString("expected_checksum"),
                                        rs.getInt("total_chunks"),
                                        rs.getString("status"),
                                        rs.getString("temp_path"),
                                        toInstant(rs.getTimestamp("expires_at"))),
                        tenantId,
                        sessionId);
        return rows.stream().findFirst();
    }

    /**
     * {@code findExpiredUploadSessions} 查询并返回当前操作所需的数据。
     *
     * @param now 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<UploadSessionRecord> findExpiredUploadSessions(Instant now) {
        Timestamp cutoff = Timestamp.from(now == null ? Instant.now() : now);
        return jdbcTemplate.query(
                "SELECT session_id, tenant_id, space_id, namespace, file_name, content_type,"
                        + " expected_size, expected_checksum, total_chunks, status, temp_path,"
                        + " expires_at FROM storage_upload_session WHERE expires_at IS NOT NULL AND"
                        + " expires_at < ? AND status IN ('UPLOADING', 'FAILED', 'CANCELLED',"
                        + " 'COMPLETED')",
                (rs, rowNum) ->
                        new UploadSessionRecord(
                                rs.getString("session_id"),
                                rs.getLong("tenant_id"),
                                rs.getObject("space_id", Long.class),
                                rs.getString("namespace"),
                                rs.getString("file_name"),
                                rs.getString("content_type"),
                                rs.getLong("expected_size"),
                                rs.getString("expected_checksum"),
                                rs.getInt("total_chunks"),
                                rs.getString("status"),
                                rs.getString("temp_path"),
                                toInstant(rs.getTimestamp("expires_at"))),
                cutoff);
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
    public void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum) {
        jdbcTemplate.update(
                dialect.upsert(
                        "storage_upload_chunk",
                        List.of(
                                "session_id",
                                "chunk_index",
                                "chunk_size",
                                "chunk_checksum",
                                "status",
                                "uploaded_at"),
                        "?, ?, ?, ?, 'UPLOADED', CURRENT_TIMESTAMP",
                        List.of("session_id", "chunk_index"),
                        List.of("chunk_size", "chunk_checksum", "status", "uploaded_at")),
                sessionId,
                chunkIndex,
                size,
                checksum);
        jdbcTemplate.update(
                "UPDATE storage_upload_session SET update_time=CURRENT_TIMESTAMP WHERE"
                        + " session_id=?",
                sessionId);
    }

    /**
     * {@code uploadedChunks} 执行当前类型定义的业务操作。
     *
     * @param sessionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<Integer> uploadedChunks(String sessionId) {
        return jdbcTemplate.query(
                "SELECT chunk_index FROM storage_upload_chunk WHERE session_id=? AND"
                        + " status='UPLOADED' ORDER BY chunk_index",
                (rs, rowNum) -> rs.getInt(1),
                sessionId);
    }

    /**
     * {@code updateUploadSessionStatus} 写入或更新当前模块中的业务数据。
     *
     * @param sessionId 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     * @param errorMessage 参数值，用于执行当前操作。
     */
    @Override
    public void updateUploadSessionStatus(String sessionId, String status, String errorMessage) {
        jdbcTemplate.update(
                "UPDATE storage_upload_session SET status=?, error_message=?,"
                        + " update_time=CURRENT_TIMESTAMP WHERE session_id=?",
                status,
                errorMessage,
                sessionId);
    }

    /**
     * {@code deleteUploadSession} 释放或移除当前操作涉及的资源。
     *
     * @param sessionId 参数值，用于执行当前操作。
     */
    @Override
    public void deleteUploadSession(String sessionId) {
        jdbcTemplate.update("DELETE FROM storage_upload_chunk WHERE session_id=?", sessionId);
        jdbcTemplate.update("DELETE FROM storage_upload_session WHERE session_id=?", sessionId);
    }

    private StorageObjectRecord mapObject(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return new StorageObjectRecord(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                rs.getObject("space_id", Long.class),
                rs.getString("namespace"),
                rs.getString("object_key"),
                rs.getString("storage_provider"),
                rs.getString("original_name"),
                rs.getString("content_type"),
                rs.getLong("file_size"),
                rs.getString("checksum"),
                rs.getString("status"),
                toInstant(rs.getTimestamp("create_time")),
                toInstant(rs.getTimestamp("update_time")));
    }

    private Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}

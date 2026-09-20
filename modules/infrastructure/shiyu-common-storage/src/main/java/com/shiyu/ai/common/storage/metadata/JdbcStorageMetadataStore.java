package com.shiyu.ai.common.storage.metadata;

import com.shiyu.ai.common.core.jdbc.JdbcDialect;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

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
 * 管理 Jdbc Storage Metadata 相关的运行时状态、注册信息或临时数据。
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
     * 执行 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbcTemplate 用于完成本次业务处理的 jdbcTemplate 参数。
     */
    public JdbcStorageMetadataStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dialect = JdbcDialect.detect(jdbcTemplate);
    }

    /**
     * 创建或保存 Jdbc Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param command 本次流程携带的事件或业务数据。
     * @return 返回 Jdbc Storage Metadata 相关操作生成的结果数据。
     */
    @Override
    public long createObject(CreateObject command) {
        requireTenant(command.tenantId());
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
     * 执行 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param objectId 用于定位object的标识。
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param size 每页返回的数据数量。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param checksum 用于完成本次业务处理的 checksum 参数。
     */
    @Override
    public void markObjectAvailable(
            long objectId,
            String objectKey,
            String provider,
            long size,
            String contentType,
            String checksum) {
        long tenantId = requireObjectTenant(objectId);
        jdbcTemplate.update(
                "UPDATE storage_object SET object_key=?, storage_provider=?, file_size=?,"
                        + " content_type=?, checksum=?, status='AVAILABLE',"
                        + " update_time=CURRENT_TIMESTAMP WHERE id=? AND tenant_id=?",
                objectKey,
                provider,
                size,
                contentType,
                checksum,
                objectId,
                tenantId);
    }

    /**
     * 执行 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param objectId 用于定位object的标识。
     * @param message 本次流程携带的事件或业务数据。
     */
    @Override
    public void markObjectFailed(long objectId, String message) {
        long tenantId = requireObjectTenant(objectId);
        jdbcTemplate.update(
                "UPDATE storage_object SET status='FAILED', metadata_json=?,"
                        + " update_time=CURRENT_TIMESTAMP WHERE id=? AND tenant_id=?",
                message,
                objectId,
                tenantId);
    }

    /**
     * 执行 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     */
    @Override
    public void markObjectDeleted(long tenantId, String objectKey) {
        requireTenant(tenantId);
        jdbcTemplate.update(
                "UPDATE storage_object SET status='DELETED', update_time=CURRENT_TIMESTAMP "
                        + "WHERE tenant_id=? AND object_key=? AND status <> 'DELETED'",
                tenantId,
                objectKey);
    }

    /**
     * 更新或设置 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     * @param provider 用于完成本次业务处理的 provider 参数。
     */
    @Override
    public void updateObjectProvider(long tenantId, String objectKey, String provider) {
        requireTenant(tenantId);
        jdbcTemplate.update(
                "UPDATE storage_object SET storage_provider=?, update_time=CURRENT_TIMESTAMP "
                        + "WHERE tenant_id=? AND object_key=? AND status='AVAILABLE'",
                provider,
                tenantId,
                objectKey);
    }

    /**
     * 查询 Jdbc Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param objectKey 用于完成本次业务处理的 objectKey 参数。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<StorageObjectRecord> findObjectByKey(long tenantId, String objectKey) {
        requireTenant(tenantId);
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
     * 查询 Jdbc Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<StorageObjectRecord> listObjects(
            long tenantId, String namespace, int offset, int limit) {
        requireTenant(tenantId);
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
     * 创建或保存 Jdbc Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param command 本次流程携带的事件或业务数据。
     * @return 返回 Jdbc Storage Metadata 相关操作生成的结果数据。
     */
    @Override
    public long createUploadSession(CreateUploadSession command) {
        requireTenant(command.tenantId());
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
     * 查询 Jdbc Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param sessionId 用于定位session的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<UploadSessionRecord> findUploadSession(long tenantId, String sessionId) {
        requireTenant(tenantId);
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
     * 查询 Jdbc Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param now 用于完成本次业务处理的 now 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     * @param chunkIndex 用于完成本次业务处理的 chunkIndex 参数。
     * @param size 每页返回的数据数量。
     * @param checksum 用于完成本次业务处理的 checksum 参数。
     */
    @Override
    public void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum) {
        long tenantId = requireSessionTenant(sessionId);
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
                        + " session_id=? AND tenant_id=?",
                sessionId,
                tenantId);
    }

    /**
     * 执行 Jdbc Storage Metadata 相关业务数据，并返回处理结果。
     *
     * @param sessionId 用于定位session的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Integer> uploadedChunks(String sessionId) {
        long tenantId = requireSessionTenant(sessionId);
        return jdbcTemplate.query(
                "SELECT chunk_index FROM storage_upload_chunk WHERE session_id=? AND"
                        + " status='UPLOADED' ORDER BY chunk_index",
                (rs, rowNum) -> rs.getInt(1),
                sessionId);
    }

    /**
     * 更新或设置 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @param errorMessage 用于完成本次业务处理的 errorMessage 参数。
     */
    @Override
    public void updateUploadSessionStatus(String sessionId, String status, String errorMessage) {
        long tenantId = requireSessionTenant(sessionId);
        jdbcTemplate.update(
                "UPDATE storage_upload_session SET status=?, error_message=?,"
                        + " update_time=CURRENT_TIMESTAMP WHERE session_id=? AND tenant_id=?",
                status,
                errorMessage,
                sessionId,
                tenantId);
    }

    /**
     * 删除或移除 Jdbc Storage Metadata 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param sessionId 用于定位session的标识。
     */
    @Override
    public void deleteUploadSession(String sessionId) {
        long tenantId = requireSessionTenant(sessionId);
        jdbcTemplate.update("DELETE FROM storage_upload_chunk WHERE session_id=?", sessionId);
        jdbcTemplate.update(
                "DELETE FROM storage_upload_session WHERE session_id=? AND tenant_id=?",
                sessionId,
                tenantId);
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

    private static void requireTenant(long tenantId) {
        TenantScope.requireMatches(new TenantId(tenantId));
    }

    private long requireObjectTenant(long objectId) {
        TenantScope.require();
        Long tenantId =
                jdbcTemplate.query(
                        "SELECT tenant_id FROM storage_object WHERE id=?",
                        rs -> rs.next() ? rs.getLong(1) : null,
                        objectId);
        if (tenantId == null) throw new IllegalArgumentException("storage object not found");
        requireTenant(tenantId);
        return tenantId;
    }

    private long requireSessionTenant(String sessionId) {
        TenantScope.require();
        Long tenantId =
                jdbcTemplate.query(
                        "SELECT tenant_id FROM storage_upload_session WHERE session_id=?",
                        rs -> rs.next() ? rs.getLong(1) : null,
                        sessionId);
        if (tenantId == null) throw new IllegalArgumentException("upload session not found");
        requireTenant(tenantId);
        return tenantId;
    }
}

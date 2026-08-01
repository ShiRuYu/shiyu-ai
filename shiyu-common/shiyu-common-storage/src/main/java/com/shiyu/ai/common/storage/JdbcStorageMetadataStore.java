package com.shiyu.ai.common.storage;

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

/** H2/MVStore metadata implementation. */
@Repository
public class JdbcStorageMetadataStore implements StorageMetadataStore {

    private final JdbcTemplate jdbcTemplate;

    public JdbcStorageMetadataStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long createObject(CreateObject command) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO storage_object (tenant_id, space_id, namespace, object_key, storage_provider, "
                            + "original_name, content_type, file_size, checksum, status, create_time, update_time) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, command.tenantId());
            if (command.spaceId() == null) statement.setObject(2, null); else statement.setLong(2, command.spaceId());
            statement.setString(3, command.namespace());
            statement.setString(4, command.objectKey());
            statement.setString(5, command.provider());
            statement.setString(6, command.originalName());
            statement.setString(7, command.contentType());
            statement.setLong(8, command.size());
            statement.setString(9, command.checksum());
            statement.setString(10, command.status());
            return statement;
        }, keyHolder);
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

    @Override
    public void markObjectAvailable(long objectId, String objectKey, String provider,
                                    long size, String contentType, String checksum) {
        jdbcTemplate.update("UPDATE storage_object SET object_key=?, storage_provider=?, file_size=?, "
                        + "content_type=?, checksum=?, status='AVAILABLE', update_time=CURRENT_TIMESTAMP WHERE id=?",
                objectKey, provider, size, contentType, checksum, objectId);
    }

    @Override
    public void markObjectFailed(long objectId, String message) {
        jdbcTemplate.update("UPDATE storage_object SET status='FAILED', metadata_json=?, update_time=CURRENT_TIMESTAMP WHERE id=?",
                message, objectId);
    }

    @Override
    public void markObjectDeleted(long tenantId, String objectKey) {
        jdbcTemplate.update("UPDATE storage_object SET status='DELETED', update_time=CURRENT_TIMESTAMP "
                        + "WHERE tenant_id=? AND object_key=? AND status <> 'DELETED'",
                tenantId, objectKey);
    }

    @Override
    public Optional<StorageObjectRecord> findObjectByKey(long tenantId, String objectKey) {
        List<StorageObjectRecord> rows = jdbcTemplate.query(
                "SELECT id, tenant_id, space_id, namespace, object_key, storage_provider, original_name, "
                        + "content_type, file_size, checksum, status, create_time, update_time "
                        + "FROM storage_object WHERE tenant_id=? AND object_key=? AND status <> 'DELETED'",
                this::mapObject, tenantId, objectKey);
        return rows.stream().findFirst();
    }

    @Override
    public List<StorageObjectRecord> listObjects(long tenantId, String namespace, int offset, int limit) {
        return jdbcTemplate.query(
                "SELECT id, tenant_id, space_id, namespace, object_key, storage_provider, original_name, "
                        + "content_type, file_size, checksum, status, create_time, update_time "
                        + "FROM storage_object WHERE tenant_id=? AND namespace=? AND status <> 'DELETED' "
                        + "ORDER BY create_time DESC LIMIT ? OFFSET ?",
                this::mapObject, tenantId, namespace, limit, offset);
    }

    @Override
    public long createUploadSession(CreateUploadSession command) {
        jdbcTemplate.update("INSERT INTO storage_upload_session (session_id, tenant_id, space_id, namespace, "
                        + "file_name, content_type, expected_size, expected_checksum, total_chunks, status, temp_path, expires_at, "
                        + "create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'UPLOADING', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                command.sessionId(), command.tenantId(), command.spaceId(), command.namespace(), command.fileName(),
                command.contentType(), command.expectedSize(), command.expectedChecksum(), command.totalChunks(),
                command.tempPath(), command.expiresAt() == null ? null : Timestamp.from(command.expiresAt()));
        return 1L;
    }

    @Override
    public Optional<UploadSessionRecord> findUploadSession(long tenantId, String sessionId) {
        List<UploadSessionRecord> rows = jdbcTemplate.query(
                "SELECT session_id, tenant_id, space_id, namespace, file_name, content_type, expected_size, "
                        + "expected_checksum, total_chunks, status, temp_path, expires_at FROM storage_upload_session "
                        + "WHERE tenant_id=? AND session_id=?",
                (rs, rowNum) -> new UploadSessionRecord(rs.getString("session_id"), rs.getLong("tenant_id"),
                        rs.getObject("space_id", Long.class), rs.getString("namespace"), rs.getString("file_name"),
                        rs.getString("content_type"), rs.getLong("expected_size"), rs.getString("expected_checksum"),
                        rs.getInt("total_chunks"), rs.getString("status"), rs.getString("temp_path"),
                        toInstant(rs.getTimestamp("expires_at"))), tenantId, sessionId);
        return rows.stream().findFirst();
    }

    @Override
    public void markChunkUploaded(String sessionId, int chunkIndex, long size, String checksum) {
        jdbcTemplate.update("MERGE INTO storage_upload_chunk (session_id, chunk_index, chunk_size, chunk_checksum, status, uploaded_at) "
                        + "KEY(session_id, chunk_index) VALUES (?, ?, ?, ?, 'UPLOADED', CURRENT_TIMESTAMP)",
                sessionId, chunkIndex, size, checksum);
        jdbcTemplate.update("UPDATE storage_upload_session SET update_time=CURRENT_TIMESTAMP WHERE session_id=?",
                sessionId);
    }

    @Override
    public List<Integer> uploadedChunks(String sessionId) {
        return jdbcTemplate.query("SELECT chunk_index FROM storage_upload_chunk WHERE session_id=? AND status='UPLOADED' "
                        + "ORDER BY chunk_index", (rs, rowNum) -> rs.getInt(1), sessionId);
    }

    @Override
    public void updateUploadSessionStatus(String sessionId, String status, String errorMessage) {
        jdbcTemplate.update("UPDATE storage_upload_session SET status=?, error_message=?, update_time=CURRENT_TIMESTAMP "
                        + "WHERE session_id=?", status, errorMessage, sessionId);
    }

    @Override
    public void deleteUploadSession(String sessionId) {
        jdbcTemplate.update("DELETE FROM storage_upload_chunk WHERE session_id=?", sessionId);
        jdbcTemplate.update("DELETE FROM storage_upload_session WHERE session_id=?", sessionId);
    }

    private StorageObjectRecord mapObject(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new StorageObjectRecord(rs.getLong("id"), rs.getLong("tenant_id"),
                rs.getObject("space_id", Long.class), rs.getString("namespace"), rs.getString("object_key"),
                rs.getString("storage_provider"), rs.getString("original_name"), rs.getString("content_type"),
                rs.getLong("file_size"), rs.getString("checksum"), rs.getString("status"),
                toInstant(rs.getTimestamp("create_time")), toInstant(rs.getTimestamp("update_time")));
    }

    private Instant toInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}

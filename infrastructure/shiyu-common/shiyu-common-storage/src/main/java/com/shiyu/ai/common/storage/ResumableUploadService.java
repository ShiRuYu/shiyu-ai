package com.shiyu.ai.common.storage;

import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumableUploadService {

    public static final int CHUNK_SIZE = 5 * 1024 * 1024;
    private static final long MAX_FILE_SIZE = 200L * 1024 * 1024;
    private static final String ID_PATTERN = "[A-Za-z0-9-]{16,64}";

    private final StorageProperties storageProperties;
    private final ObjectStorage objectStorage;
    private final ContentSecurityScanner securityScanner;
    private final ResumableUploadHandler uploadHandler;
    private final StorageMetadataStore metadataStore;

    public UploadSession begin(Long spaceId, BeginRequest request) {
        if (request == null || request.fileName() == null || request.fileName().isBlank()) {
            throw new ServiceException("文件名不能为空");
        }
        if (request.size() <= 0 || request.size() > MAX_FILE_SIZE) {
            throw new ServiceException("文件大小必须在 1 字节至 200 MB 之间");
        }
        Long tenantId = currentTenant();
        uploadHandler.authorize(tenantId, spaceId);
        try {
            String id = UUID.randomUUID().toString();
            Path directory = directory(id);
            Files.createDirectories(directory);
            Properties properties = new Properties();
            properties.setProperty("tenantId", tenantId.toString());
            properties.setProperty("spaceId", spaceId.toString());
            properties.setProperty("namespace", uploadHandler.namespace(tenantId, spaceId));
            properties.setProperty("fileName", safeFileName(request.fileName()));
            properties.setProperty("contentType", request.contentType() == null ? "application/octet-stream" : request.contentType());
            properties.setProperty("size", Long.toString(request.size()));
            properties.setProperty("totalChunks", Integer.toString(totalChunks(request.size())));
            properties.setProperty("checksum", request.checksum() == null ? "" : request.checksum().trim().toLowerCase());
            properties.setProperty("title", request.title() == null ? "" : request.title().trim());
            properties.setProperty("expiresAt", Instant.now().plusSeconds(24 * 3600).toString());
            try (OutputStream output = Files.newOutputStream(metadata(id), StandardOpenOption.CREATE_NEW)) {
                properties.store(output, "storage resumable upload");
            }
            if (metadataStore.persistent()) {
                metadataStore.createUploadSession(new StorageMetadataStore.CreateUploadSession(
                        id, tenantId, spaceId, properties.getProperty("namespace"),
                        properties.getProperty("fileName"), properties.getProperty("contentType"),
                        request.size(), properties.getProperty("checksum"),
                        Integer.parseInt(properties.getProperty("totalChunks")),
                        directory(id).toString(), java.time.Instant.now().plusSeconds(24 * 3600)));
            }
            return session(id, properties);
        } catch (IOException exception) {
            throw new ServiceException("创建上传会话失败: " + exception.getMessage());
        }
    }

    public UploadSession status(String id) {
        if (metadataStore.persistent()) {
            StorageMetadataStore.UploadSessionRecord record = metadataStore.findUploadSession(currentTenant(), id)
                    .orElseThrow(() -> new ServiceException("上传会话不存在"));
            return session(record);
        }
        Properties properties = loadForCurrentTenant(id);
        return session(id, properties);
    }

    public UploadSession writeChunk(String id, int index, int totalChunks, byte[] bytes) {
        Properties properties = loadForCurrentTenant(id);
        int expectedTotal = Integer.parseInt(properties.getProperty("totalChunks"));
        if (totalChunks != expectedTotal || index < 0 || index >= expectedTotal) {
            throw new ServiceException("分片序号或总数不正确");
        }
        if (bytes == null || bytes.length == 0 || bytes.length > CHUNK_SIZE) {
            throw new ServiceException("分片大小必须大于 0 且不超过 5 MB");
        }
        try {
            Files.write(part(id, index), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            if (metadataStore.persistent()) {
                metadataStore.markChunkUploaded(id, index, bytes.length, sha256(bytes));
            }
            return session(id, properties);
        } catch (IOException exception) {
            throw new ServiceException("保存分片失败: " + exception.getMessage());
        }
    }

    public ResumableUploadHandler.RegistrationResult complete(String id) {
        Properties properties = loadForCurrentTenant(id);
        int totalChunks = Integer.parseInt(properties.getProperty("totalChunks"));
        long expectedSize = Long.parseLong(properties.getProperty("size"));
        ObjectStorage.StoredObject stored = null;
        try {
            for (int index = 0; index < totalChunks; index++) {
                if (!Files.isRegularFile(part(id, index))) {
                    throw new ServiceException("分片未全部上传，缺少第 " + index + " 片");
                }
            }
            Path merged = directory(id).resolve("merged");
            try (OutputStream output = Files.newOutputStream(merged, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (int index = 0; index < totalChunks; index++) {
                    Files.copy(part(id, index), output);
                }
            }
            if (Files.size(merged) != expectedSize) {
                throw new ServiceException("合并后的文件大小不匹配");
            }
            byte[] content = Files.readAllBytes(merged);
            String checksum = sha256(content);
            String expectedChecksum = properties.getProperty("checksum", "");
            if (!expectedChecksum.isBlank() && !expectedChecksum.equalsIgnoreCase(checksum)) {
                throw new ServiceException("文件校验值不匹配，请重新上传");
            }
            String fileName = properties.getProperty("fileName");
            String contentType = properties.getProperty("contentType");
            securityScanner.validate(fileName, contentType, content);
            Long tenantId = Long.valueOf(properties.getProperty("tenantId"));
            Long spaceId = Long.valueOf(properties.getProperty("spaceId"));
            stored = objectStorage.put(properties.getProperty("namespace"),
                    fileName, contentType, content.length, new ByteArrayInputStream(content));
            try {
                ResumableUploadHandler.RegistrationResult result = uploadHandler.register(
                        new ResumableUploadHandler.UploadRegistration(tenantId, spaceId,
                                properties.getProperty("title").isBlank() ? fileName : properties.getProperty("title"),
                                fileName, stored.objectKey(), stored.provider(), stored.contentType(),
                                stored.size(), checksum));
                if (result.duplicate()) objectStorage.delete(stored.objectKey());
                if (metadataStore.persistent()) {
                    metadataStore.updateUploadSessionStatus(id, "COMPLETED", null);
                }
                cleanup(id);
                return result;
            } catch (RuntimeException exception) {
                if (metadataStore.persistent()) {
                    metadataStore.updateUploadSessionStatus(id, "FAILED", exception.getMessage());
                }
                deleteQuietly(stored);
                throw exception;
            }
        } catch (ServiceException exception) {
            markFailed(id, exception.getMessage());
            deleteQuietly(stored);
            throw exception;
        } catch (IOException exception) {
            markFailed(id, exception.getMessage());
            deleteQuietly(stored);
            throw new ServiceException("合并上传文件失败: " + exception.getMessage());
        }
    }

    public void cancel(String id) {
        if (metadataStore.persistent()) {
            StorageMetadataStore.UploadSessionRecord record = metadataStore.findUploadSession(currentTenant(), id)
                    .orElseThrow(() -> new ServiceException("上传会话不存在"));
            metadataStore.updateUploadSessionStatus(record.sessionId(), "CANCELLED", null);
        } else {
            loadForCurrentTenant(id);
        }
        try {
            cleanup(id);
        } catch (IOException exception) {
            throw new ServiceException("取消上传失败: " + exception.getMessage());
        }
    }

    /** Removes expired database sessions and orphaned local chunk directories. */
    @Scheduled(fixedDelayString = "${shiyu.storage.upload.cleanup-interval-ms:3600000}",
            initialDelayString = "${shiyu.storage.upload.cleanup-initial-delay-ms:300000}")
    void cleanupExpiredSessions() {
        Instant now = Instant.now();
        for (StorageMetadataStore.UploadSessionRecord record : metadataStore.findExpiredUploadSessions(now)) {
            deleteWithinChunkRoot(record.tempPath());
            metadataStore.deleteUploadSession(record.sessionId());
        }
        if (!"local".equalsIgnoreCase(storageProperties.getType())) return;
        try {
            Path chunkRoot = root();
            try (var directories = Files.list(chunkRoot)) {
                directories.filter(Files::isDirectory).forEach(directory -> {
                    Path metadata = directory.resolve("metadata.properties");
                    if (!Files.isRegularFile(metadata)) return;
                    try (InputStream input = Files.newInputStream(metadata)) {
                        Properties properties = new Properties();
                        properties.load(input);
                        String expiresAt = properties.getProperty("expiresAt");
                        if (expiresAt != null && !expiresAt.isBlank()
                                && Instant.parse(expiresAt).isBefore(now)) {
                            deleteDirectory(directory, chunkRoot);
                        }
                    } catch (Exception exception) {
                        try {
                            if (Files.getLastModifiedTime(metadata).toInstant()
                                    .plusSeconds(7 * 24 * 3600).isBefore(now)) {
                                deleteDirectory(directory, chunkRoot);
                            }
                        } catch (IOException ignored) {
                            // Best-effort cleanup; the next scheduled pass retries it.
                        }
                    }
                });
            }
        } catch (IOException exception) {
            throw new ServiceException("清理过期断点上传失败: " + exception.getMessage());
        }
    }

    private UploadSession session(String id, Properties properties) {
        int total = Integer.parseInt(properties.getProperty("totalChunks"));
        List<Integer> uploaded = new ArrayList<>();
        try {
            for (int index = 0; index < total; index++) {
                if (Files.isRegularFile(part(id, index))) uploaded.add(index);
            }
        } catch (IOException exception) {
            throw new ServiceException("读取上传进度失败: " + exception.getMessage());
        }
        return new UploadSession(id, Long.valueOf(properties.getProperty("spaceId")),
                properties.getProperty("fileName"), Long.parseLong(properties.getProperty("size")),
                total, uploaded, CHUNK_SIZE);
    }

    private UploadSession session(StorageMetadataStore.UploadSessionRecord record) {
        List<Integer> uploaded = metadataStore.uploadedChunks(record.sessionId());
        return new UploadSession(record.sessionId(), record.spaceId(), record.fileName(), record.expectedSize(),
                record.totalChunks(), uploaded, CHUNK_SIZE);
    }

    private Properties loadForCurrentTenant(String id) {
        if (id == null || !id.matches(ID_PATTERN)) throw new ServiceException("上传会话不存在");
        try {
            Properties properties = new Properties();
            try {
                try (InputStream input = Files.newInputStream(metadata(id))) {
                    properties.load(input);
                }
            } catch (java.nio.file.NoSuchFileException missingMetadata) {
                if (!metadataStore.persistent()) throw missingMetadata;
                StorageMetadataStore.UploadSessionRecord record = metadataStore.findUploadSession(currentTenant(), id)
                        .orElseThrow(() -> new ServiceException("上传会话不存在"));
                properties.setProperty("tenantId", Long.toString(record.tenantId()));
                properties.setProperty("spaceId", Long.toString(record.spaceId()));
                properties.setProperty("namespace", record.namespace());
                properties.setProperty("fileName", record.fileName());
                properties.setProperty("contentType", record.contentType());
                properties.setProperty("size", Long.toString(record.expectedSize()));
                properties.setProperty("totalChunks", Integer.toString(record.totalChunks()));
                properties.setProperty("checksum", record.expectedChecksum() == null ? "" : record.expectedChecksum());
            }
            if (!properties.getProperty("tenantId").equals(currentTenant().toString())) {
                throw new ServiceException("无权访问该上传会话");
            }
            uploadHandler.authorize(currentTenant(), Long.valueOf(properties.getProperty("spaceId")));
            return properties;
        } catch (IOException | NumberFormatException exception) {
            throw new ServiceException("上传会话不存在");
        }
    }

    private Path root() throws IOException {
        if (!"local".equalsIgnoreCase(storageProperties.getType())) {
            throw new ServiceException("当前仅支持本地存储的断点上传");
        }
        Path path = Path.of(storageProperties.getLocal().getPath()).toAbsolutePath().normalize().resolve(".chunks");
        Files.createDirectories(path);
        return path;
    }

    private Path directory(String id) throws IOException { return root().resolve(id).normalize(); }
    private Path metadata(String id) throws IOException { return directory(id).resolve("metadata.properties"); }
    private Path part(String id, int index) throws IOException { return directory(id).resolve("part-" + index); }

    private void deleteWithinChunkRoot(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()
                || !"local".equalsIgnoreCase(storageProperties.getType())) return;
        try {
            deleteDirectory(Path.of(configuredPath), root());
        } catch (IOException ignored) {
            // The next orphan scan retries locked or missing paths.
        }
    }

    private void deleteDirectory(Path candidate, Path root) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!normalized.startsWith(normalizedRoot) || normalized.equals(normalizedRoot)) return;
        if (!Files.exists(normalized)) return;
        try (var paths = Files.walk(normalized)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (IOException ignored) { }
            });
        }
    }

    private void cleanup(String id) throws IOException {
        Path directory = directory(id);
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (IOException ignored) { }
                });
            }
        }
    }

    private void deleteQuietly(ObjectStorage.StoredObject stored) {
        if (stored == null) {
            return;
        }
        try {
            objectStorage.delete(stored.objectKey());
        } catch (IOException ignored) {
            // Preserve the registration error; the orphan can be reconciled from storage metadata.
        }
    }

    private void markFailed(String id, String message) {
        if (metadataStore.persistent()) {
            metadataStore.updateUploadSessionStatus(id, "FAILED", message);
        }
    }

    private int totalChunks(long size) { return (int) ((size + CHUNK_SIZE - 1) / CHUNK_SIZE); }
    private Long currentTenant() {
        Long tenantId = UserContextHolder.getCurrentTenantId();
        if (tenantId == null) throw new ServiceException("当前租户上下文不存在");
        return tenantId;
    }
    private String safeFileName(String value) {
        String name = value.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).trim();
        if (name.isBlank() || name.contains("..")) throw new ServiceException("文件名不合法");
        return name;
    }
    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record BeginRequest(String fileName, String contentType, long size,
                               String checksum, String title) { }
    public record UploadSession(String sessionId, Long spaceId, String fileName,
                                long size, int totalChunks, List<Integer> uploadedChunks,
                                int chunkSize) { }
}

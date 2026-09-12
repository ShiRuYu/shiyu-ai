package com.shiyu.ai.common.storage.file.service;
import com.shiyu.ai.common.storage.file.port.ResumableUploadHandler;

import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

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

/**
 * {@code ResumableUploadService} 定义平台基础设施模块的应用服务能力，供上层用例调用。
 */
@Service
@RequiredArgsConstructor
public class ResumableUploadService {

    /**
     * 大小，表示当前对象中的对应属性。
     */
    public static final int CHUNK_SIZE = 5 * 1024 * 1024;
    /**
     * 大小，表示当前对象中的对应属性。
     */
    private static final long MAX_FILE_SIZE = 200L * 1024 * 1024;
    /**
     * 匹配模式，表示当前对象中的对应属性。
     */
    private static final String ID_PATTERN = "[A-Za-z0-9-]{16,64}";

    /**
     * storageProperties 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final StorageProperties storageProperties;
    /**
     * objectStorage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ObjectStorage objectStorage;
    /**
     * securityScanner 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ContentSecurityScanner securityScanner;
    /**
     * uploadHandler 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ResumableUploadHandler uploadHandler;
    /**
     * 元数据存储，表示当前对象中的对应属性。
     */
    private final StorageMetadataStore metadataStore;

    /**
     * {@code begin} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param spaceId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public UploadSession begin(
            ResumableUploadHandler.UploadActor actor, Long spaceId, BeginRequest request) {
        if (request == null || request.fileName() == null || request.fileName().isBlank()) {
            throw new ServiceException("文件名不能为空");
        }
        if (request.size() <= 0 || request.size() > MAX_FILE_SIZE) {
            throw new ServiceException("文件大小必须在 1 字节至 200 MB 之间");
        }
        requireActor(actor);
        TenantId tenantId = actor.tenantId();
        uploadHandler.authorize(actor, spaceId);
        try {
            String id = UUID.randomUUID().toString();
            Path directory = directory(id);
            Files.createDirectories(directory);
            Properties properties = new Properties();
            properties.setProperty("tenantId", Long.toString(tenantId.value()));
            properties.setProperty("spaceId", spaceId.toString());
            properties.setProperty("namespace", uploadHandler.namespace(tenantId, spaceId));
            properties.setProperty("fileName", safeFileName(request.fileName()));
            properties.setProperty(
                    "contentType",
                    request.contentType() == null
                            ? "application/octet-stream"
                            : request.contentType());
            properties.setProperty("size", Long.toString(request.size()));
            properties.setProperty("totalChunks", Integer.toString(totalChunks(request.size())));
            properties.setProperty(
                    "checksum",
                    request.checksum() == null ? "" : request.checksum().trim().toLowerCase());
            properties.setProperty("title", request.title() == null ? "" : request.title().trim());
            properties.setProperty("expiresAt", Instant.now().plusSeconds(24 * 3600).toString());
            try (OutputStream output =
                    Files.newOutputStream(metadata(id), StandardOpenOption.CREATE_NEW)) {
                properties.store(output, "storage resumable upload");
            }
            if (metadataStore.persistent()) {
                metadataStore.createUploadSession(
                        new StorageMetadataStore.CreateUploadSession(
                                id,
                                tenantId.value(),
                                spaceId,
                                properties.getProperty("namespace"),
                                properties.getProperty("fileName"),
                                properties.getProperty("contentType"),
                                request.size(),
                                properties.getProperty("checksum"),
                                Integer.parseInt(properties.getProperty("totalChunks")),
                                directory(id).toString(),
                                java.time.Instant.now().plusSeconds(24 * 3600)));
            }
            return session(id, properties);
        } catch (IOException exception) {
            throw new ServiceException("创建上传会话失败");
        }
    }

    /**
     * {@code status} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public UploadSession status(ResumableUploadHandler.UploadActor actor, String id) {
        requireActor(actor);
        TenantId tenantId = actor.tenantId();
        if (metadataStore.persistent()) {
            StorageMetadataStore.UploadSessionRecord record =
                    metadataStore
                            .findUploadSession(tenantId.value(), id)
                            .orElseThrow(() -> new ServiceException("上传会话不存在"));
            return session(record);
        }
        Properties properties = loadForActor(actor, id);
        return session(id, properties);
    }

    /**
     * {@code writeChunk} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     * @param index 参数值，用于执行当前操作。
     * @param totalChunks 参数值，用于执行当前操作。
     * @param bytes 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public UploadSession writeChunk(
            ResumableUploadHandler.UploadActor actor,
            String id,
            int index,
            int totalChunks,
            byte[] bytes) {
        Properties properties = loadForActor(actor, id);
        int expectedTotal = Integer.parseInt(properties.getProperty("totalChunks"));
        if (totalChunks != expectedTotal || index < 0 || index >= expectedTotal) {
            throw new ServiceException("分片序号或总数不正确");
        }
        if (bytes == null || bytes.length == 0 || bytes.length > CHUNK_SIZE) {
            throw new ServiceException("分片大小必须大于 0 且不超过 5 MB");
        }
        try {
            Files.write(
                    part(id, index),
                    bytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            if (metadataStore.persistent()) {
                metadataStore.markChunkUploaded(id, index, bytes.length, sha256(bytes));
            }
            return session(id, properties);
        } catch (IOException exception) {
            throw new ServiceException("保存分片失败");
        }
    }

    /**
     * {@code complete} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ResumableUploadHandler.RegistrationResult complete(
            ResumableUploadHandler.UploadActor actor, String id) {
        Properties properties = loadForActor(actor, id);
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
            try (OutputStream output =
                    Files.newOutputStream(
                            merged,
                            StandardOpenOption.CREATE,
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
            TenantId sessionTenantId =
                    new TenantId(Long.parseLong(properties.getProperty("tenantId")));
            Long spaceId = Long.valueOf(properties.getProperty("spaceId"));
            stored =
                    objectStorage.put(
                            properties.getProperty("namespace"),
                            fileName,
                            contentType,
                            content.length,
                            new ByteArrayInputStream(content));
            try {
                ResumableUploadHandler.RegistrationResult result =
                        uploadHandler.register(
                                actor,
                                new ResumableUploadHandler.UploadRegistration(
                                        sessionTenantId,
                                        spaceId,
                                        properties.getProperty("title").isBlank()
                                                ? fileName
                                                : properties.getProperty("title"),
                                        fileName,
                                        stored.objectKey(),
                                        stored.provider(),
                                        stored.contentType(),
                                        stored.size(),
                                        checksum));
                if (result.duplicate()) objectStorage.delete(stored.objectKey());
                if (metadataStore.persistent()) {
                    metadataStore.updateUploadSessionStatus(id, "COMPLETED", null);
                }
                cleanup(id);
                return result;
            } catch (RuntimeException exception) {
                if (metadataStore.persistent()) {
                    metadataStore.updateUploadSessionStatus(id, "FAILED", "上传文件失败");
                }
                deleteQuietly(stored);
                throw exception;
            }
        } catch (ServiceException exception) {
            markFailed(id, exception.getMessage());
            deleteQuietly(stored);
            throw exception;
        } catch (IOException exception) {
            markFailed(id, "合并上传文件失败");
            deleteQuietly(stored);
            throw new ServiceException("合并上传文件失败");
        }
    }

    /**
     * {@code cancel} 校验当前操作的输入或状态是否满足约束。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param id 参数值，用于执行当前操作。
     */
    public void cancel(ResumableUploadHandler.UploadActor actor, String id) {
        requireActor(actor);
        TenantId tenantId = actor.tenantId();
        if (metadataStore.persistent()) {
            StorageMetadataStore.UploadSessionRecord record =
                    metadataStore
                            .findUploadSession(tenantId.value(), id)
                            .orElseThrow(() -> new ServiceException("上传会话不存在"));
            metadataStore.updateUploadSessionStatus(record.sessionId(), "CANCELLED", null);
        } else {
            loadForActor(actor, id);
        }
        try {
            cleanup(id);
        } catch (IOException exception) {
            throw new ServiceException("取消上传失败");
        }
    }

    /**
     * {@code cleanupExpiredSessions} 执行当前类型定义的业务操作。
     * 定期清理过期的分片上传会话和临时文件。
     */
   @Scheduled(
           fixedDelayString = "${shiyu.storage.upload.cleanup-interval-ms:3600000}",
           initialDelayString = "${shiyu.storage.upload.cleanup-initial-delay-ms:300000}")
   public void cleanupExpiredSessions() {
       Instant now = Instant.now();
        for (StorageMetadataStore.UploadSessionRecord record :
                metadataStore.findExpiredUploadSessions(now)) {
            deleteWithinChunkRoot(record.tempPath());
            metadataStore.deleteUploadSession(record.sessionId());
        }
        if (!"local".equalsIgnoreCase(storageProperties.getType())) return;
        try {
            Path chunkRoot = root();
            try (var directories = Files.list(chunkRoot)) {
                directories
                        .filter(Files::isDirectory)
                        .forEach(
                                directory -> {
                                    Path metadata = directory.resolve("metadata.properties");
                                    if (!Files.isRegularFile(metadata)) return;
                                    try (InputStream input = Files.newInputStream(metadata)) {
                                        Properties properties = new Properties();
                                        properties.load(input);
                                        String expiresAt = properties.getProperty("expiresAt");
                                        if (expiresAt != null
                                                && !expiresAt.isBlank()
                                                && Instant.parse(expiresAt).isBefore(now)) {
                                            deleteDirectory(directory, chunkRoot);
                                        }
                                    } catch (Exception exception) {
                                        try {
                                            if (Files.getLastModifiedTime(metadata)
                                                    .toInstant()
                                                    .plusSeconds(7 * 24 * 3600)
                                                    .isBefore(now)) {
                                                deleteDirectory(directory, chunkRoot);
                                            }
                                        } catch (IOException ignored) {
                                            // it.
                                        }
                                    }
                                });
            }
        } catch (IOException exception) {
            throw new ServiceException("清理过期断点上传失败");
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
            throw new ServiceException("读取上传进度失败");
        }
        return new UploadSession(
                id,
                Long.valueOf(properties.getProperty("spaceId")),
                properties.getProperty("fileName"),
                Long.parseLong(properties.getProperty("size")),
                total,
                uploaded,
                CHUNK_SIZE);
    }

    private UploadSession session(StorageMetadataStore.UploadSessionRecord record) {
        List<Integer> uploaded = metadataStore.uploadedChunks(record.sessionId());
        return new UploadSession(
                record.sessionId(),
                record.spaceId(),
                record.fileName(),
                record.expectedSize(),
                record.totalChunks(),
                uploaded,
                CHUNK_SIZE);
    }

    private Properties loadForActor(ResumableUploadHandler.UploadActor actor, String id) {
        requireActor(actor);
        TenantId tenantId = actor.tenantId();
        if (id == null || !id.matches(ID_PATTERN)) throw new ServiceException("上传会话不存在");
        try {
            Properties properties = new Properties();
            try {
                try (InputStream input = Files.newInputStream(metadata(id))) {
                    properties.load(input);
                }
            } catch (java.nio.file.NoSuchFileException missingMetadata) {
                if (!metadataStore.persistent()) throw missingMetadata;
                StorageMetadataStore.UploadSessionRecord record =
                        metadataStore
                                .findUploadSession(tenantId.value(), id)
                                .orElseThrow(() -> new ServiceException("上传会话不存在"));
                properties.setProperty("tenantId", Long.toString(record.tenantId()));
                properties.setProperty("spaceId", Long.toString(record.spaceId()));
                properties.setProperty("namespace", record.namespace());
                properties.setProperty("fileName", record.fileName());
                properties.setProperty("contentType", record.contentType());
                properties.setProperty("size", Long.toString(record.expectedSize()));
                properties.setProperty("totalChunks", Integer.toString(record.totalChunks()));
                properties.setProperty(
                        "checksum",
                        record.expectedChecksum() == null ? "" : record.expectedChecksum());
            }
            if (!properties.getProperty("tenantId").equals(Long.toString(tenantId.value()))) {
                throw new ServiceException("无权访问该上传会话");
            }
            uploadHandler.authorize(actor, Long.valueOf(properties.getProperty("spaceId")));
            return properties;
        } catch (IOException | NumberFormatException exception) {
            throw new ServiceException("上传会话不存在");
        }
    }

    private Path root() throws IOException {
        if (!"local".equalsIgnoreCase(storageProperties.getType())) {
            throw new ServiceException("当前仅支持本地存储的断点上传");
        }
        Path path =
                Path.of(storageProperties.getLocal().getPath())
                        .toAbsolutePath()
                        .normalize()
                        .resolve(".chunks");
        Files.createDirectories(path);
        return path;
    }

    private Path directory(String id) throws IOException {
        return root().resolve(id).normalize();
    }

    private Path metadata(String id) throws IOException {
        return directory(id).resolve("metadata.properties");
    }

    private Path part(String id, int index) throws IOException {
        return directory(id).resolve("part-" + index);
    }

    private void deleteWithinChunkRoot(String configuredPath) {
        if (configuredPath == null
                || configuredPath.isBlank()
                || !"local".equalsIgnoreCase(storageProperties.getType())) return;
        try {
            deleteDirectory(Path.of(configuredPath), root());
        } catch (IOException ignored) {
        }
    }

    private void deleteDirectory(Path candidate, Path root) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!normalized.startsWith(normalizedRoot) || normalized.equals(normalizedRoot)) return;
        if (!Files.exists(normalized)) return;
        try (var paths = Files.walk(normalized)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(
                            path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            });
        }
    }

    private void cleanup(String id) throws IOException {
        Path directory = directory(id);
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(
                                path -> {
                                    try {
                                        Files.deleteIfExists(path);
                                    } catch (IOException ignored) {
                                    }
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
        }
    }

    private void markFailed(String id, String message) {
        if (metadataStore.persistent()) {
            metadataStore.updateUploadSessionStatus(id, "FAILED", message);
        }
    }

    private int totalChunks(long size) {
        return (int) ((size + CHUNK_SIZE - 1) / CHUNK_SIZE);
    }

    private static void requireActor(ResumableUploadHandler.UploadActor actor) {
        if (actor == null) {
            throw new ServiceException("合法租户与用户标识不能为空");
        }
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

    /**
     * {@code BeginRequest} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param fileName 文件名，表示该记录组件承载的数据。
     * @param contentType 内容类型，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param checksum checksum 属性，表示该记录组件承载的数据。
     * @param title 标题，表示该记录组件承载的数据。
     */
    public record BeginRequest(
            @NotBlank(message = "文件名不能为空") String fileName,
            String contentType,
            @Positive(message = "文件大小必须大于 0")
                    @Max(value = MAX_FILE_SIZE, message = "文件大小不能超过 200 MB")
                    long size,
            String checksum,
            String title) {}

    /**
     * {@code UploadSession} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param sessionId sessionId 属性，表示该记录组件承载的数据。
     * @param spaceId spaceId 属性，表示该记录组件承载的数据。
     * @param fileName 文件名，表示该记录组件承载的数据。
     * @param size 大小，表示该记录组件承载的数据。
     * @param totalChunks totalChunks 属性，表示该记录组件承载的数据。
     * @param uploadedChunks uploadedChunks 属性，表示该记录组件承载的数据。
     * @param chunkSize chunkSize 属性，表示该记录组件承载的数据。
     */
    public record UploadSession(
            String sessionId,
            Long spaceId,
            String fileName,
            long size,
            int totalChunks,
            List<Integer> uploadedChunks,
            int chunkSize) {}
}

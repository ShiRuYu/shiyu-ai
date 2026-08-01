package com.shiyu.ai.web.knowledge;

import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.exception.ServiceException;
import com.shiyu.ai.knowledge.service.EnterpriseDocumentService;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import com.shiyu.ai.knowledge.storage.ContentSecurityScanner;
import com.shiyu.ai.knowledge.storage.ObjectStorage;
import com.shiyu.ai.web.common.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private final EnterpriseDocumentService documentService;
    private final KnowledgeSpaceService spaceService;

    public UploadSession begin(Long spaceId, BeginRequest request) {
        if (request == null || request.fileName() == null || request.fileName().isBlank()) {
            throw new ServiceException("文件名不能为空");
        }
        if (request.size() <= 0 || request.size() > MAX_FILE_SIZE) {
            throw new ServiceException("文件大小必须在 1 字节至 200 MB 之间");
        }
        Long tenantId = currentTenant();
        spaceService.requireAccess(spaceId, KnowledgeSpaceService.SpaceRole.EDITOR);
        try {
            String id = UUID.randomUUID().toString();
            Path directory = directory(id);
            Files.createDirectories(directory);
            Properties properties = new Properties();
            properties.setProperty("tenantId", tenantId.toString());
            properties.setProperty("spaceId", spaceId.toString());
            properties.setProperty("fileName", safeFileName(request.fileName()));
            properties.setProperty("contentType", request.contentType() == null ? "application/octet-stream" : request.contentType());
            properties.setProperty("size", Long.toString(request.size()));
            properties.setProperty("totalChunks", Integer.toString(totalChunks(request.size())));
            properties.setProperty("checksum", request.checksum() == null ? "" : request.checksum().trim().toLowerCase());
            properties.setProperty("title", request.title() == null ? "" : request.title().trim());
            try (OutputStream output = Files.newOutputStream(metadata(id), StandardOpenOption.CREATE_NEW)) {
                properties.store(output, "knowledge resumable upload");
            }
            return session(id, properties);
        } catch (IOException exception) {
            throw new ServiceException("创建上传会话失败: " + exception.getMessage());
        }
    }

    public UploadSession status(String id) {
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
            return session(id, properties);
        } catch (IOException exception) {
            throw new ServiceException("保存分片失败: " + exception.getMessage());
        }
    }

    public EnterpriseDocumentService.UploadResult complete(String id) {
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
            stored = objectStorage.put("knowledge/" + tenantId + "/" + spaceId,
                    fileName, contentType, content.length, new ByteArrayInputStream(content));
            try {
                EnterpriseDocumentService.UploadResult result = documentService.registerStoredFile(
                        new EnterpriseDocumentService.StoredFileRequest(spaceId,
                                properties.getProperty("title").isBlank() ? fileName : properties.getProperty("title"),
                                fileName, stored.objectKey(), stored.provider(), stored.contentType(),
                                stored.size(), checksum));
                if (result.duplicate()) objectStorage.delete(stored.objectKey());
                cleanup(id);
                return result;
            } catch (RuntimeException exception) {
                deleteQuietly(stored);
                throw exception;
            }
        } catch (IOException exception) {
            throw new ServiceException("合并上传文件失败: " + exception.getMessage());
        }
    }

    public void cancel(String id) {
        loadForCurrentTenant(id);
        try {
            cleanup(id);
        } catch (IOException exception) {
            throw new ServiceException("取消上传失败: " + exception.getMessage());
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

    private Properties loadForCurrentTenant(String id) {
        if (id == null || !id.matches(ID_PATTERN)) throw new ServiceException("上传会话不存在");
        try {
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(metadata(id))) {
                properties.load(input);
            }
            if (!properties.getProperty("tenantId").equals(currentTenant().toString())) {
                throw new ServiceException("无权访问该上传会话");
            }
            spaceService.requireAccess(Long.valueOf(properties.getProperty("spaceId")), KnowledgeSpaceService.SpaceRole.EDITOR);
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

    private int totalChunks(long size) { return (int) ((size + CHUNK_SIZE - 1) / CHUNK_SIZE); }
    private Long currentTenant() {
        Long tenantId = LoginContextHolder.getCurrentTenantId();
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

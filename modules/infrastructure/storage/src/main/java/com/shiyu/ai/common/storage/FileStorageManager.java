package com.shiyu.ai.common.storage;

import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HexFormat;

public class FileStorageManager implements AutoCloseable {

    public static final Set<String> SUPPORTED_TYPES =
            Set.of("local", "s3", "minio", "aliyun-oss", "tencent-cos");

    private final String type;
    private final FileStorage storage;
    private final StorageMetadataStore metadataStore;

    public FileStorageManager(StorageProperties properties) throws IOException {
        this(properties, NoopStorageMetadataStore.INSTANCE);
    }

    public FileStorageManager(StorageProperties properties, StorageMetadataStore metadataStore) throws IOException {
        this.type = normalizeType(properties.getType());
        this.metadataStore = metadataStore == null ? NoopStorageMetadataStore.INSTANCE : metadataStore;
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new IllegalStateException("不支持的文件存储方式: " + type);
        }
        if ("local".equals(type)) {
            String path = properties.getLocal().getPath();
            if (!StringUtils.hasText(path)) {
                throw new IllegalStateException("本地存储路径不能为空");
            }
            this.storage = new LocalFileStorage(Path.of(path), type);
        } else {
            StorageProperties.S3Provider provider = properties.getProviders().get(type);
            if (provider == null) {
                throw new IllegalStateException("未配置文件存储提供商: shiyu.storage.providers." + type);
            }
            this.storage = new S3CompatibleFileStorage(type, provider);
        }
    }

    public String type() {
        return type;
    }

    public StoredFile upload(
            String namespace, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException {
        String normalizedNamespace = normalizeNamespace(namespace);
        java.security.DigestInputStream digestInput = new java.security.DigestInputStream(
                inputStream, digest());
        StoredFile stored = storage.upload(normalizedNamespace, originalName, contentType, size, digestInput);
        if (!metadataStore.persistent()) return stored;
        long tenantId = tenantId(normalizedNamespace);
        Long spaceId = spaceId(normalizedNamespace);
        String checksum = HexFormat.of().formatHex(digestInput.getMessageDigest().digest());
        long metadataId = metadataStore.createObject(new StorageMetadataStore.CreateObject(
                tenantId, spaceId, normalizedNamespace, stored.name(), stored.key(), stored.storageType(),
                stored.contentType(), stored.size(), checksum, "AVAILABLE"));
        if (metadataId <= 0) {
            try { storage.delete(stored.key()); } catch (IOException ignored) { }
            throw new IOException("文件记录写入数据库失败");
        }
        return new StoredFile(stored.key(), stored.name(), stored.size(), stored.contentType(),
                stored.lastModified(), stored.url(), stored.storageType());
    }

    public List<StoredFile> list(String namespace) throws IOException {
        String normalizedNamespace = normalizeNamespace(namespace);
        if (!metadataStore.persistent()) return storage.list(normalizedNamespace);
        long tenantId = tenantId(normalizedNamespace);
        return metadataStore.listObjects(tenantId, normalizedNamespace, 0, 1000).stream()
                .map(record -> new StoredFile(record.objectKey(), record.originalName(), record.size(),
                        record.contentType(), record.updateTime(), null, record.provider()))
                .toList();
    }

    public StorageObject open(String key) throws IOException {
        if (metadataStore.persistent()) {
            long tenantId = tenantIdFromKey(key);
            StorageMetadataStore.StorageObjectRecord record = metadataStore.findObjectByKey(tenantId, key)
                    .orElseThrow(() -> new FileNotFoundException("文件记录不存在"));
            if (!"AVAILABLE".equals(record.status())) {
                throw new IOException("文件当前不可用: " + record.status());
            }
        }
        return storage.open(key);
    }

    public void delete(String key) throws IOException {
        storage.delete(key);
        if (metadataStore.persistent()) {
            metadataStore.markObjectDeleted(tenantIdFromKey(key), key);
        }
    }

    @Override
    public void close() {
        if (storage instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception exception) {
                // Backend shutdown is best-effort during application shutdown.
            }
        }
    }

    private String normalizeType(String value) {
        return value == null || value.isBlank() ? "local" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNamespace(String namespace) {
        if (namespace == null || namespace.isBlank()
                || namespace.contains("..") || namespace.startsWith("/") || namespace.startsWith("\\")) {
            throw new IllegalArgumentException("非法文件命名空间");
        }
        return namespace.replace('\\', '/').replaceAll("/+$", "") + "/";
    }

    private long tenantId(String namespace) throws IOException {
        String[] parts = namespace.split("/");
        if (parts.length < 2) {
            throw new IOException("存储命名空间缺少租户标识");
        }
        for (String part : parts) {
            try {
                return Long.parseLong(part);
            } catch (NumberFormatException ignored) {
                // Namespace prefixes belong to the caller; storage only needs
                // the tenant segment for metadata isolation.
            }
        }
        throw new IOException("存储命名空间租户标识无效");
    }

    private long tenantIdFromKey(String key) throws IOException {
        if (key == null) throw new IOException("文件标识不能为空");
        return tenantId(key);
    }

    private Long spaceId(String namespace) {
        int numericSegments = 0;
        for (String part : namespace.split("/")) {
            try {
                Long value = Long.parseLong(part);
                if (numericSegments++ == 1) return value;
            } catch (NumberFormatException ignored) {
                // Caller-defined namespace labels are intentionally opaque.
            }
        }
        return null;
    }

    private MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}

package com.shiyu.ai.common.storage;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileStorageManager implements AutoCloseable {

    public static final Set<String> SUPPORTED_TYPES =
            Set.of("local", "s3", "minio", "aliyun-oss", "tencent-cos");

    private final String type;
    private final FileStorage storage;

    public FileStorageManager(StorageProperties properties) throws IOException {
        this.type = normalizeType(properties.getType());
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
        return storage.upload(normalizeNamespace(namespace), originalName, contentType, size, inputStream);
    }

    public List<StoredFile> list(String namespace) throws IOException {
        return storage.list(normalizeNamespace(namespace));
    }

    public StorageObject open(String key) throws IOException {
        return storage.open(key);
    }

    public void delete(String key) throws IOException {
        storage.delete(key);
    }

    @Override
    public void close() {
        if (storage instanceof S3CompatibleFileStorage s3Storage) {
            s3Storage.close();
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
}

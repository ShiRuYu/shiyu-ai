package com.shiyu.ai.common.storage.file.service;
import com.shiyu.ai.common.storage.file.adapter.LocalFileStorage;
import com.shiyu.ai.common.storage.file.adapter.S3CompatibleFileStorage;
import com.shiyu.ai.common.storage.file.port.KeyedFileStorage;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 管理 文件 Storage 相关的运行时状态、注册信息或临时数据。
 */
public class FileStorageManager implements AutoCloseable {

    public static final Set<String> SUPPORTED_TYPES =
            Set.of("local", "s3", "minio", "aliyun-oss", "tencent-cos");

    /**
     * 类型，表示当前对象中的对应属性。
     */
    private final String type;
    /**
     * storage 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final FileStorage storage;
    /**
     * 元数据存储，表示当前对象中的对应属性。
     */
    private final StorageMetadataStore metadataStore;

    /**
     * 执行 文件 Storage 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param properties 用于完成本次业务处理的 properties 参数。
     */
    public FileStorageManager(StorageProperties properties) throws IOException {
        this(properties, NoopStorageMetadataStore.INSTANCE);
    }

    /**
     * 执行 文件 Storage 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param properties 用于完成本次业务处理的 properties 参数。
     * @param metadataStore 用于完成本次业务处理的 metadataStore 参数。
     */
    public FileStorageManager(StorageProperties properties, StorageMetadataStore metadataStore)
            throws IOException {
        this.type = normalizeType(properties.getType());
        this.metadataStore =
                metadataStore == null ? NoopStorageMetadataStore.INSTANCE : metadataStore;
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

    /**
     * 执行 文件 Storage 相关业务数据，并返回处理结果。
     *
     * @return 返回 文件 Storage 相关操作生成的结果数据。
     */
    public String type() {
        return type;
    }

    /**
     * 执行 文件 Storage 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param size 每页返回的数据数量。
     * @param inputStream 用于完成本次业务处理的 inputStream 参数。
     * @return 返回 文件 Storage 相关操作生成的结果数据。
     */
    public StoredFile upload(
            String namespace,
            String originalName,
            String contentType,
            long size,
            InputStream inputStream)
            throws IOException {
        String normalizedNamespace = normalizeNamespace(namespace);
        java.security.DigestInputStream digestInput =
                new java.security.DigestInputStream(inputStream, digest());
        StoredFile stored =
                storage.upload(normalizedNamespace, originalName, contentType, size, digestInput);
        if (!metadataStore.persistent()) return stored;
        long tenantId = tenantId(normalizedNamespace);
        Long spaceId = spaceId(normalizedNamespace);
        String checksum = HexFormat.of().formatHex(digestInput.getMessageDigest().digest());
        long metadataId =
                metadataStore.createObject(
                        new StorageMetadataStore.CreateObject(
                                tenantId,
                                spaceId,
                                normalizedNamespace,
                                stored.name(),
                                stored.key(),
                                stored.storageType(),
                                stored.contentType(),
                                stored.size(),
                                checksum,
                                "AVAILABLE"));
        if (metadataId <= 0) {
            try {
                storage.delete(stored.key());
            } catch (IOException ignored) {
            }
            throw new IOException("文件记录写入数据库失败");
        }
        return new StoredFile(
                stored.key(),
                stored.name(),
                stored.size(),
                stored.contentType(),
                stored.lastModified(),
                stored.url(),
                stored.storageType());
    }

    /**
     * 按存储键上传文件。
     *
     * @param key key 参数。
     * @param originalName originalName 参数。
     * @param contentType contentType 参数。
     * @param size size 参数。
     * @param inputStream inputStream 参数。
     *
     * @return 处理结果。
     */
    public StoredFile uploadAtKey(
            String key, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException {
        if (!(storage instanceof KeyedFileStorage keyedStorage)) {
            throw new IOException("当前文件存储不支持保留对象 key 的迁移");
        }
        validateKey(key);
        return keyedStorage.uploadAtKey(key, originalName, contentType, size, inputStream);
    }

    /**
     * 查询 文件 Storage 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<StoredFile> list(String namespace) throws IOException {
        String normalizedNamespace = normalizeNamespace(namespace);
        if (!metadataStore.persistent()) return storage.list(normalizedNamespace);
        long tenantId = tenantId(normalizedNamespace);
        return metadataStore.listObjects(tenantId, normalizedNamespace, 0, 1000).stream()
                .map(
                        record ->
                                new StoredFile(
                                        record.objectKey(),
                                        record.originalName(),
                                        record.size(),
                                        record.contentType(),
                                        record.updateTime(),
                                        null,
                                        record.provider()))
                .toList();
    }

    /**
     * 创建或保存 文件 Storage 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 文件 Storage 相关操作生成的结果数据。
     */
    public StorageObject open(String key) throws IOException {
        if (metadataStore.persistent()) {
            long tenantId = tenantIdFromKey(key);
            StorageMetadataStore.StorageObjectRecord record =
                    metadataStore
                            .findObjectByKey(tenantId, key)
                            .orElseThrow(() -> new FileNotFoundException("文件记录不存在"));
            if (!"AVAILABLE".equals(record.status())) {
                throw new IOException("文件当前不可用: " + record.status());
            }
        }
        return storage.open(key);
    }

    /**
     * 删除或移除 文件 Storage 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     */
    public void delete(String key) throws IOException {
        storage.delete(key);
        if (metadataStore.persistent()) {
            metadataStore.markObjectDeleted(tenantIdFromKey(key), key);
        }
    }

    /**
     * {@code close} 释放或移除当前操作涉及的资源。
     */
    @Override
    public void close() {
        if (storage instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception exception) {
            }
        }
    }

    private String normalizeType(String value) {
        return value == null || value.isBlank() ? "local" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNamespace(String namespace) {
        if (namespace == null
                || namespace.isBlank()
                || namespace.contains("..")
                || namespace.startsWith("/")
                || namespace.startsWith("\\")) {
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
            }
        }
        throw new IOException("存储命名空间租户标识无效");
    }

    private long tenantIdFromKey(String key) throws IOException {
        if (key == null) throw new IOException("文件标识不能为空");
        return tenantId(key);
    }

    private void validateKey(String key) throws IOException {
        if (key == null
                || key.isBlank()
                || key.startsWith("/")
                || key.startsWith("\\")
                || key.contains("..")
                || key.contains("\\")) {
            throw new IOException("非法文件标识");
        }
    }

    private Long spaceId(String namespace) {
        int numericSegments = 0;
        for (String part : namespace.split("/")) {
            try {
                Long value = Long.parseLong(part);
                if (numericSegments++ == 1) return value;
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

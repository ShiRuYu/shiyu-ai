package com.shiyu.ai.common.storage.file.adapter;

import com.shiyu.ai.common.storage.file.key.StorageKeys;
import com.shiyu.ai.common.storage.file.port.KeyedFileStorage;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;

/**
 * 负责 Local 文件 相关数据的存储或后台处理。
 */
public class LocalFileStorage implements KeyedFileStorage {

    /**
     * 根目录，表示当前对象中的对应属性。
     */
    private final Path root;
    /**
     * storageType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String storageType;

    /**
     * 执行 Local 文件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param root 用于完成本次业务处理的 root 参数。
     * @param storageType 用于完成本次业务处理的 storageType 参数。
     */
    public LocalFileStorage(Path root, String storageType) throws IOException {
        this.root = root.toAbsolutePath().normalize();
        this.storageType = storageType;
        Files.createDirectories(this.root);
    }

    /**
     * 执行 Local 文件 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param size 每页返回的数据数量。
     * @param inputStream 用于完成本次业务处理的 inputStream 参数。
     * @return 返回 Local 文件 相关操作生成的结果数据。
     */
    @Override
    public StoredFile upload(
            String namespace,
            String originalName,
            String contentType,
            long size,
            InputStream inputStream)
            throws IOException {
        String key = StorageKeys.create(namespace, originalName);
        return uploadAtKey(key, originalName, contentType, size, inputStream);
    }

    /**
     * 执行 Local 文件 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param originalName 用于完成本次业务处理的 originalName 参数。
     * @param contentType 用于完成本次业务处理的 contentType 参数。
     * @param size 每页返回的数据数量。
     * @param inputStream 用于完成本次业务处理的 inputStream 参数。
     * @return 返回 Local 文件 相关操作生成的结果数据。
     */
    @Override
    public StoredFile uploadAtKey(
            String key, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return toStoredFile(key, target);
    }

    /**
     * 查询 Local 文件 相关业务数据，并返回处理结果。
     *
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<StoredFile> list(String namespace) throws IOException {
        Path namespaceRoot = resolve(namespace);
        if (!Files.isDirectory(namespaceRoot)) {
            return List.of();
        }
        try (var paths = Files.walk(namespaceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .map(
                            path ->
                                    toStoredFileUnchecked(
                                            root.relativize(path).toString().replace('\\', '/'),
                                            path))
                    .sorted(Comparator.comparing(StoredFile::lastModified).reversed())
                    .toList();
        }
    }

    /**
     * 创建或保存 Local 文件 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Local 文件 相关操作生成的结果数据。
     */
    @Override
    public StorageObject open(String key) throws IOException {
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            throw new FileNotFoundException("文件不存在");
        }
        return new StorageObject(
                Files.newInputStream(path),
                StorageKeys.originalName(key),
                detectContentType(path, StorageKeys.originalName(key)),
                Files.size(path));
    }

    /**
     * 删除或移除 Local 文件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     */
    @Override
    public void delete(String key) throws IOException {
        Path path = resolve(key);
        if (!Files.deleteIfExists(path)) {
            throw new FileNotFoundException("文件不存在");
        }
        removeEmptyParents(path.getParent());
    }

    private StoredFile toStoredFile(String key, Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        String originalName = StorageKeys.originalName(key);
        return new StoredFile(
                key,
                originalName,
                attributes.size(),
                detectContentType(path, originalName),
                attributes.lastModifiedTime().toInstant(),
                null,
                storageType);
    }

    private StoredFile toStoredFileUnchecked(String key, Path path) {
        try {
            return toStoredFile(key, path);
        } catch (IOException ex) {
            throw new IllegalStateException("读取本地文件信息失败: " + path, ex);
        }
    }

    private Path resolve(String key) throws IOException {
        if (key == null || key.isBlank()) {
            throw new IOException("文件标识不能为空");
        }
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new IOException("非法文件标识");
        }
        return path;
    }

    private String detectContentType(Path path, String originalName) throws IOException {
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = Files.probeContentType(Path.of(originalName));
        }
        return contentType == null ? "application/octet-stream" : contentType;
    }

    private void removeEmptyParents(Path directory) throws IOException {
        Path current = directory;
        while (current != null && !current.equals(root)) {
            try (var children = Files.list(current)) {
                if (children.findAny().isPresent()) {
                    return;
                }
            }
            Files.deleteIfExists(current);
            current = current.getParent();
        }
    }
}

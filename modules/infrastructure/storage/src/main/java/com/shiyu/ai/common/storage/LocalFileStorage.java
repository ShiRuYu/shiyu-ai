package com.shiyu.ai.common.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class LocalFileStorage implements FileStorage {

    private final Path root;
    private final String storageType;

    public LocalFileStorage(Path root, String storageType) throws IOException {
        this.root = root.toAbsolutePath().normalize();
        this.storageType = storageType;
        Files.createDirectories(this.root);
    }

    @Override
    public StoredFile upload(
            String namespace, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException {
        String key = StorageKeys.create(namespace, originalName);
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return toStoredFile(key, target);
    }

    @Override
    public List<StoredFile> list(String namespace) throws IOException {
        Path namespaceRoot = resolve(namespace);
        if (!Files.isDirectory(namespaceRoot)) {
            return List.of();
        }
        try (var paths = Files.walk(namespaceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> toStoredFileUnchecked(root.relativize(path).toString().replace('\\', '/'), path))
                    .sorted(Comparator.comparing(StoredFile::lastModified).reversed())
                    .toList();
        }
    }

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

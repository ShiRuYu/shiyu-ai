package com.shiyu.ai.common.storage.file.adapter;
import com.shiyu.ai.common.storage.file.port.KeyedFileStorage;

import com.shiyu.ai.common.storage.api.*;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * {@code S3CompatibleFileStorage} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public class S3CompatibleFileStorage implements KeyedFileStorage, AutoCloseable {

    /**
     * storageType 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String storageType;
    /**
     * 配置属性，表示当前对象中的对应属性。
     */
    private final StorageProperties.S3Provider properties;
    /**
     * 客户端，表示当前对象中的对应属性。
     */
    private final S3Client client;

    /**
     * {@code S3CompatibleFileStorage} 创建并初始化当前类型实例。
     *
     * @param storageType 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     */
    public S3CompatibleFileStorage(String storageType, StorageProperties.S3Provider properties) {
        this.storageType = storageType;
        this.properties = properties;
        validate();

        var builder =
                S3Client.builder()
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(
                                                properties.getAccessKey(),
                                                properties.getSecretKey())))
                        .region(Region.of(properties.getRegion()))
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                                        .build());
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }
        this.client = builder.build();
    }

    /**
     * {@code upload} 执行当前类型定义的业务操作。
     *
     * @param namespace 参数值，用于执行当前操作。
     * @param originalName 参数值，用于执行当前操作。
     * @param contentType 参数值，用于执行当前操作。
     * @param size 参数值，用于执行当前操作。
     * @param inputStream 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code uploadAtKey} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     * @param originalName 参数值，用于执行当前操作。
     * @param contentType 参数值，用于执行当前操作。
     * @param size 参数值，用于执行当前操作。
     * @param inputStream 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public StoredFile uploadAtKey(
            String key, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException {
        try {
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.getBucket())
                            .key(key)
                            .contentType(normalizeContentType(contentType, originalName))
                            .metadata(Map.of("original-name", StorageKeys.originalName(key)))
                            .build(),
                    RequestBody.fromInputStream(inputStream, size));
            return new StoredFile(
                    key,
                    StorageKeys.originalName(key),
                    size,
                    normalizeContentType(contentType, originalName),
                    java.time.Instant.now(),
                    publicUrl(key),
                    storageType);
        } catch (S3Exception ex) {
            throw new IOException("外部存储上传失败", ex);
        }
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param namespace 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<StoredFile> list(String namespace) throws IOException {
        try {
            return client
                    .listObjectsV2Paginator(
                            ListObjectsV2Request.builder()
                                    .bucket(properties.getBucket())
                                    .prefix(namespace)
                                    .build())
                    .contents()
                    .stream()
                    .map(
                            item -> {
                                String originalName = StorageKeys.originalName(item.key());
                                return new StoredFile(
                                        item.key(),
                                        originalName,
                                        item.size(),
                                        normalizeContentType(null, originalName),
                                        item.lastModified(),
                                        publicUrl(item.key()),
                                        storageType);
                            })
                    .sorted(java.util.Comparator.comparing(StoredFile::lastModified).reversed())
                    .toList();
        } catch (S3Exception ex) {
            throw new IOException("读取外部存储文件列表失败", ex);
        }
    }

    /**
     * {@code open} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public StorageObject open(String key) throws IOException {
        try {
            ResponseInputStream<GetObjectResponse> response =
                    client.getObject(
                            GetObjectRequest.builder()
                                    .bucket(properties.getBucket())
                                    .key(key)
                                    .build());
            GetObjectResponse metadata = response.response();
            String originalName =
                    metadata.metadata()
                            .getOrDefault("original-name", StorageKeys.originalName(key));
            return new StorageObject(
                    response,
                    originalName,
                    normalizeContentType(metadata.contentType(), originalName),
                    metadata.contentLength());
        } catch (NoSuchKeyException ex) {
            FileNotFoundException notFound = new FileNotFoundException("文件不存在");
            notFound.initCause(ex);
            throw notFound;
        } catch (S3Exception ex) {
            throw new IOException("读取外部存储文件失败", ex);
        }
    }

    /**
     * {@code delete} 释放或移除当前操作涉及的资源。
     *
     * @param key 参数值，用于执行当前操作。
     */
    @Override
    public void delete(String key) throws IOException {
        try {
            client.deleteObject(
                    DeleteObjectRequest.builder().bucket(properties.getBucket()).key(key).build());
        } catch (S3Exception ex) {
            throw new IOException("删除外部存储文件失败", ex);
        }
    }

    /**
     * {@code close} 释放或移除当前操作涉及的资源。
     */
    @Override
    public void close() {
        client.close();
    }

    private void validate() {
        if (properties.getBucket() == null
                || properties.getBucket().isBlank()
                || properties.getAccessKey() == null
                || properties.getAccessKey().isBlank()
                || properties.getSecretKey() == null
                || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException(
                    "存储方式 " + storageType + " 缺少 bucket/access-key/secret-key 配置");
        }
    }

    private String publicUrl(String key) {
        String baseUrl = properties.getPublicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        return baseUrl.replaceAll("/+$", "") + "/" + key;
    }

    private String normalizeContentType(String contentType, String originalName) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }
        try {
            String detected = Files.probeContentType(Path.of(originalName));
            return detected == null ? "application/octet-stream" : detected;
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }
}

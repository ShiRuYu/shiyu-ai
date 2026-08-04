package com.shiyu.ai.common.storage;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class S3CompatibleFileStorage implements FileStorage, AutoCloseable {

    private final String storageType;
    private final StorageProperties.S3Provider properties;
    private final S3Client client;

    public S3CompatibleFileStorage(String storageType, StorageProperties.S3Provider properties) {
        this.storageType = storageType;
        this.properties = properties;
        validate();

        var builder = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .region(Region.of(properties.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .build());
        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }
        this.client = builder.build();
    }

    @Override
    public StoredFile upload(
            String namespace, String originalName, String contentType, long size, InputStream inputStream)
            throws IOException {
        String key = StorageKeys.create(namespace, originalName);
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
            throw new IOException("外部存储上传失败: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public List<StoredFile> list(String namespace) throws IOException {
        try {
            return client.listObjectsV2Paginator(ListObjectsV2Request.builder()
                            .bucket(properties.getBucket())
                            .prefix(namespace)
                            .build())
                    .contents()
                    .stream()
                    .map(item -> {
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
            throw new IOException("读取外部存储文件列表失败: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public StorageObject open(String key) throws IOException {
        try {
            ResponseInputStream<GetObjectResponse> response = client.getObject(
                    GetObjectRequest.builder().bucket(properties.getBucket()).key(key).build());
            GetObjectResponse metadata = response.response();
            String originalName = metadata.metadata().getOrDefault("original-name", StorageKeys.originalName(key));
            return new StorageObject(
                    response,
                    originalName,
                    normalizeContentType(metadata.contentType(), originalName),
                    metadata.contentLength());
        } catch (NoSuchKeyException ex) {
            throw new IOException("文件不存在", ex);
        } catch (S3Exception ex) {
            throw new IOException("读取外部存储文件失败: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build());
        } catch (S3Exception ex) {
            throw new IOException("删除外部存储文件失败: " + ex.awsErrorDetails().errorMessage(), ex);
        }
    }

    @Override
    public void close() {
        client.close();
    }

    private void validate() {
        if (properties.getBucket() == null || properties.getBucket().isBlank()
                || properties.getAccessKey() == null || properties.getAccessKey().isBlank()
                || properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException("存储方式 " + storageType + " 缺少 bucket/access-key/secret-key 配置");
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

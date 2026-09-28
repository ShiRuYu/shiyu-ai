package com.shiyu.ai.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiyu.ai.common.storage.api.StorageObject;
import com.shiyu.ai.common.storage.config.StorageProperties;
import com.shiyu.ai.common.storage.file.adapter.S3CompatibleFileStorage;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * 验证 RustFS 提供的 S3 兼容容器相关功能、边界条件、异常路径和协作行为。
 */
@Testcontainers(disabledWithoutDocker = true)
class MinioContainerSmokeTest {

    private static final String ACCESS_KEY = "SHIYUACCESSKEY";
    private static final String SECRET_KEY = "SHIYUSECRETKEY";
    private static final String IMAGE =
            "rustfs/rustfs:1.0.0-glibc@sha256:bffcab0c9d647aab0055d1c69d340b202d0909966b385932d4ead1aeb7602858";

    @Container
    static final GenericContainer<?> STORAGE =
            new GenericContainer<>(IMAGE)
                    .withEnv("RUSTFS_ACCESS_KEY", ACCESS_KEY)
                    .withEnv("RUSTFS_SECRET_KEY", SECRET_KEY)
                    .withCommand("/data")
                    .withExposedPorts(9000);

    @Test
    void preservesObjectKeysAcrossUploadReadListAndDelete() throws Exception {
        String endpoint = "http://" + STORAGE.getHost() + ":" + STORAGE.getMappedPort(9000);
        try (S3Client admin = s3Client(endpoint)) {
            admin.createBucket(CreateBucketRequest.builder().bucket("shiyu-test").build());
        }

        StorageProperties.S3Provider properties = new StorageProperties.S3Provider();
        properties.setEndpoint(endpoint);
        properties.setBucket("shiyu-test");
        properties.setAccessKey(ACCESS_KEY);
        properties.setSecretKey(SECRET_KEY);
        properties.setPathStyleAccess(true);
        try (S3CompatibleFileStorage storage = new S3CompatibleFileStorage("minio", properties)) {
            String key = "tenant-1/docs/report.txt";
            storage.uploadAtKey(
                    key,
                    "report.txt",
                    "text/plain",
                    5,
                    new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
            assertThat(storage.list("tenant-1/docs/"))
                    .singleElement()
                    .extracting(item -> item.key())
                    .isEqualTo(key);
            StorageObject object = storage.open(key);
            try (var input = object.inputStream()) {
                assertThat(input.readAllBytes())
                        .isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
            }
            storage.delete(key);
            assertThat(storage.list("tenant-1/docs/")).isEmpty();
        }
    }

    private static S3Client s3Client(String endpoint) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}

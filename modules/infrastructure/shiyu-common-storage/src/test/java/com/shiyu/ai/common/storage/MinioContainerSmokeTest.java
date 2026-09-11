package com.shiyu.ai.common.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiyu.ai.common.storage.api.StorageObject;
import com.shiyu.ai.common.storage.config.StorageProperties;
import com.shiyu.ai.common.storage.file.S3CompatibleFileStorage;

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

/** Verifies the S3-compatible provider against MinIO when Docker is available. */
@Testcontainers(disabledWithoutDocker = true)
class MinioContainerSmokeTest {

    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin123";

    @Container
    static final GenericContainer<?> MINIO =
            new GenericContainer<>("minio/minio:RELEASE.2024-11-07T00-52-20Z")
                    .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
                    .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
                    .withCommand("server /data --console-address :9001")
                    .withExposedPorts(9000);

    @Test
    void preservesObjectKeysAcrossUploadReadListAndDelete() throws Exception {
        String endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000);
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

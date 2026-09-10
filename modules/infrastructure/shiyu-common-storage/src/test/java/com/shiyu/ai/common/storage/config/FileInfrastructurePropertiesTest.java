package com.shiyu.ai.common.storage.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileInfrastructurePropertiesTest {

    @Test
    void blankProviderKeepsLegacyStorageType() {
        FileInfrastructureProperties properties = new FileInfrastructureProperties();

        assertThat(properties.resolveProvider("s3")).isEqualTo("s3");
    }

    @Test
    void configuredProviderOverridesLegacyStorageType() {
        FileInfrastructureProperties properties = new FileInfrastructureProperties();
        properties.setProvider("minio");

        assertThat(properties.resolveProvider("local")).isEqualTo("minio");
    }
}

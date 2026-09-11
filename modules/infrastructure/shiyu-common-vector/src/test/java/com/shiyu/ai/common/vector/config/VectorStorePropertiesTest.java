package com.shiyu.ai.common.vector.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VectorStorePropertiesTest {

    @Test
    void blankProviderKeepsLegacyVectorStoreType() {
        VectorInfrastructureProperties properties = new VectorInfrastructureProperties();

        assertThat(properties.resolveProvider("jvector")).isEqualTo("jvector");
    }

    @Test
    void configuredProviderOverridesLegacyVectorStoreType() {
        VectorInfrastructureProperties properties = new VectorInfrastructureProperties();
        properties.setProvider("pgvector");

        assertThat(properties.resolveProvider("jvector")).isEqualTo("pgvector");
    }
}

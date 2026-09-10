package com.shiyu.ai.common.mybatis.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatabaseInfrastructurePropertiesTest {

    @Test
    void defaultsToH2AndNormalizesProvider() {
        DatabaseInfrastructureProperties properties = new DatabaseInfrastructureProperties();

        assertThat(properties.getProvider()).isEqualTo("h2");
        properties.setProvider(" PostgreSQL ");
        assertThat(properties.normalizedProvider()).isEqualTo("postgresql");
    }

    @Test
    void rejectsUnknownProvider() {
        DatabaseInfrastructureProperties properties = new DatabaseInfrastructureProperties();
        properties.setProvider("oracle");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("oracle");
    }
}

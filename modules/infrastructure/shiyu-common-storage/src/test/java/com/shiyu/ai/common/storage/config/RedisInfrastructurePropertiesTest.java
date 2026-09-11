package com.shiyu.ai.common.storage.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RedisInfrastructurePropertiesTest {

    @Test
    void disabledProviderDoesNotRequireConnectionDetails() {
        RedisInfrastructureProperties properties = new RedisInfrastructureProperties();

        assertThat(properties.normalizedProvider()).isEqualTo("disabled");
        assertThat(properties.key("tenant", "lock")).isEqualTo("shiyu:tenant:lock");
    }

    @Test
    void redisProviderUsesConfiguredPrefixAndNormalizesKeys() {
        RedisInfrastructureProperties properties = new RedisInfrastructureProperties();
        properties.setProvider(" Redis ");
        properties.setKeyPrefix("app");

        assertThat(properties.normalizedProvider()).isEqualTo("redis");
        assertThat(properties.key("tenant/1", "lock/1")).isEqualTo("app:tenant/1:lock/1");
    }

    @Test
    void validationRejectsUnknownProviderAndMissingUrl() {
        RedisInfrastructureProperties properties = new RedisInfrastructureProperties();
        properties.setProvider("memcached");
        assertThatThrownBy(properties::validate).isInstanceOf(IllegalStateException.class);

        properties.setProvider("redis");
        properties.setUrl(" ");
        assertThatThrownBy(properties::validate).isInstanceOf(IllegalStateException.class);
    }
}

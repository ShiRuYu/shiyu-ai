package com.shiyu.ai.common.event.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EventInfrastructurePropertiesTest {

    @Test
    void defaultsToInProcessAndNormalizesProvider() {
        EventInfrastructureProperties properties = new EventInfrastructureProperties();

        assertThat(properties.normalizedProvider()).isEqualTo("in-process");
        properties.setProvider(" KAFKA ");
        assertThat(properties.normalizedProvider()).isEqualTo("kafka");
    }
}

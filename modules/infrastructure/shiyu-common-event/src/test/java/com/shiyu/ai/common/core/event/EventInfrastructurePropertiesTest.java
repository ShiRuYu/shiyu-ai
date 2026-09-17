package com.shiyu.ai.common.core.event;

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

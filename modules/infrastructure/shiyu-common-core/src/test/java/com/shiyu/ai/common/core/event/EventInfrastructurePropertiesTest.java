package com.shiyu.ai.common.core.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventInfrastructurePropertiesTest {

    @Test
    void defaultsToInProcessAndNormalizesProvider() {
        EventInfrastructureProperties properties = new EventInfrastructureProperties();

        assertThat(properties.normalizedProvider()).isEqualTo("in-process");
        properties.setProvider(" KAFKA ");
        assertThat(properties.normalizedProvider()).isEqualTo("kafka");
    }
}

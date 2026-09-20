package com.shiyu.ai.common.event.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 验证 事件 Infrastructure Properties 相关功能、边界条件、异常路径和协作行为。
 */
class EventInfrastructurePropertiesTest {

    @Test
    void defaultsToInProcessAndNormalizesProvider() {
        EventInfrastructureProperties properties = new EventInfrastructureProperties();

        assertThat(properties.normalizedProvider()).isEqualTo("in-process");
        properties.setProvider(" KAFKA ");
        assertThat(properties.normalizedProvider()).isEqualTo("kafka");
    }
}

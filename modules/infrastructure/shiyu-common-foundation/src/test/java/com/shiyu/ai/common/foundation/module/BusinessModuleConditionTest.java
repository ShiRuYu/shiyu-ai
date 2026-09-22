package com.shiyu.ai.common.foundation.module;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

/**
 * 验证 Business Module Condition 相关功能、边界条件、异常路径和协作行为。
 */
class BusinessModuleConditionTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(EnabledModule.class, DisabledByDefaultModule.class);

    @Test
    void enablesWhenMissingAndMatchIfMissingIsTrue() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(EnabledModule.class));
    }

    @Test
    void disablesWhenMissingAndMatchIfMissingIsFalse() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(DisabledByDefaultModule.class));
    }

    @Test
    void honorsCanonicalProperty() {
        contextRunner
                .withPropertyValues("shiyu.modules.enabled.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(EnabledModule.class));
    }

    @Test
    void honorsEnvironmentVariableStyleProperty() {
        contextRunner
                .withPropertyValues("SHIYU_MODULE_ENABLED_ENABLED=false")
                .run(context -> assertThat(context).doesNotHaveBean(EnabledModule.class));
    }

    @Test
    void rejectsInvalidBoolean() {
        contextRunner
                .withPropertyValues("shiyu.modules.enabled.enabled=maybe")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsInvalidModuleId() {
        assertThatThrownBy(
                        () ->
                                BusinessModuleDescriptor.of(
                                        "Education_Module", "教育", "/api/education", "edu:"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * 验证 Enabled Module 相关功能、边界条件、异常路径和协作行为。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBusinessModule(value = "enabled", matchIfMissing = true)

    static class EnabledModule {}

    /**
     * 验证 Disabled By Default Module 相关功能、边界条件、异常路径和协作行为。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBusinessModule(value = "disabled-by-default", matchIfMissing = false)

    static class DisabledByDefaultModule {}
}

package com.shiyu.ai.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 验证 Single Application Composition 相关功能、边界条件、异常路径和协作行为。
 */
class SingleApplicationCompositionTest {

    @Test
    void startupScansOnlyTheCompositionRoot() {
        SpringBootApplication annotation =
                ShiyuBootstrapApplication.class.getAnnotation(SpringBootApplication.class);

        assertThat(annotation.scanBasePackages())
                .containsExactly("com.shiyu.ai.bootstrap", "com.shiyu.ai.composition");
    }

    @Test
    void platformBootstrapIsNotAnAdditionalApplication() {
        assertThatThrownBy(
                        () ->
                                Class.forName(
                                        "com.shiyu.ai.platform.bootstrap.PlatformBootstrapApplication"))
                .isInstanceOf(ClassNotFoundException.class);
    }

    @Test
    void startupClassHasOnlyOneMainEntryPoint() {
        long mainMethods =
                java.util.Arrays.stream(ShiyuBootstrapApplication.class.getDeclaredMethods())
                        .filter(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                        .filter(method -> method.getName().equals("main"))
                        .count();
        assertThat(mainMethods).isEqualTo(1);
    }
}

package com.shiyu.ai.common.thread.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.shiyu.ai.common.thread.api.TaskDecorator;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ThreadingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(ThreadingAutoConfiguration.class))
                    .withPropertyValues(
                            "shiyu.thread.metrics-enabled=false",
                            "shiyu.thread.otel-enabled=false",
                            "shiyu.thread.pools.knowledge-ingestion.type=IO_INTENSIVE",
                            "shiyu.thread.pools.knowledge-ingestion.core-size=2",
                            "shiyu.thread.pools.knowledge-ingestion.max-size=2");

    @Test
    void createsSingleManagedThreadPoolWithoutObservabilityBackends() {
        contextRunner.run(
                context -> {
                    assertThat(context).hasSingleBean(ThreadPoolManager.class);
                    assertThat(context).hasSingleBean(TaskDecorator.class);
                    assertThat(context).doesNotHaveBean("otelTaskDecorator");
                    assertThat(
                                    context.getBean(ThreadPoolManager.class)
                                            .getExecutor("knowledge-ingestion"))
                            .isNotNull();
                });
    }
}

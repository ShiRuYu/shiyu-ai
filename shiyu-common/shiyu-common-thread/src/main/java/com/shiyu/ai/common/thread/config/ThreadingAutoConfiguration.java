package com.shiyu.ai.common.thread.config;

import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import com.shiyu.ai.common.thread.api.TaskDecorator;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.common.thread.context.ContextTaskDecorator;
import com.shiyu.ai.common.thread.core.CompositeTaskDecorator;
import com.shiyu.ai.common.thread.core.DefaultThreadPoolManager;
import com.shiyu.ai.common.thread.metrics.MicrometerExecutorBinder;
import com.shiyu.ai.common.thread.otel.OtelTaskDecorator;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;

/** Auto-configuration for managed background worker executors. */
@AutoConfiguration
@EnableConfigurationProperties(ThreadingProperties.class)
@PropertySource(value = "classpath:application-thread-default.yml", factory = YmlPropertySourceFactory.class)
public class ThreadingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ThreadingAutoConfiguration.class);

    @Bean
    public TaskDecorator threadTaskDecorator(ObjectProvider<OtelTaskDecorator> otelDecorator) {
        List<TaskDecorator> decorators = new ArrayList<>();
        decorators.add(new ContextTaskDecorator());
        otelDecorator.ifAvailable(decorators::add);
        return new CompositeTaskDecorator(decorators);
    }

    @Bean(destroyMethod = "shutdownAll")
    @ConditionalOnMissingBean(ThreadPoolManager.class)
    public ThreadPoolManager threadPoolManager(
            ThreadingProperties properties,
            @Qualifier("threadTaskDecorator") TaskDecorator taskDecorator) {
        logger.info("Creating managed worker thread pool manager");
        return new DefaultThreadPoolManager(properties, taskDecorator);
    }

    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "shiyu.thread", name = "metrics-enabled",
            havingValue = "true", matchIfMissing = false)
    public SmartLifecycle threadPoolMetricsLifecycle(
            ThreadPoolManager threadPoolManager,
            MeterRegistry meterRegistry,
            ThreadingProperties properties) {
        return new SmartLifecycle() {
            private volatile boolean running;

            @Override
            public void start() {
                properties.getPools().forEach((name, ignored) -> {
                    new MicrometerExecutorBinder(threadPoolManager.getExecutor(name), name)
                            .bindTo(meterRegistry);
                });
                running = true;
                logger.info("Managed worker thread pool metrics enabled");
            }

            @Override
            public void stop() {
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }
        };
    }

    @Bean
    @ConditionalOnClass(Tracer.class)
    @ConditionalOnBean(OpenTelemetry.class)
    @ConditionalOnProperty(prefix = "shiyu.thread", name = "otel-enabled",
            havingValue = "true", matchIfMissing = false)
    public OtelTaskDecorator otelTaskDecorator(OpenTelemetry openTelemetry) {
        logger.info("OpenTelemetry propagation enabled for managed worker threads");
        return new OtelTaskDecorator(openTelemetry.getTracer("shiyu-threading", "1.0.0"));
    }
}

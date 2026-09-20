package com.shiyu.ai.common.thread.config;

import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import com.shiyu.ai.common.thread.api.TaskDecorator;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.common.thread.context.CompositeTaskDecorator;
import com.shiyu.ai.common.thread.context.ContextTaskDecorator;
import com.shiyu.ai.common.thread.executor.DefaultThreadPoolManager;
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

/**
 * 定义 Threading Auto 基础设施或应用能力的配置项及装配规则。
 */
@AutoConfiguration
@EnableConfigurationProperties(ThreadingProperties.class)
@PropertySource(
        value = "classpath:application-thread-default.yml",
        factory = YmlPropertySourceFactory.class)
public class ThreadingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ThreadingAutoConfiguration.class);

    /**
     * 执行 Threading Auto 相关业务数据，并返回处理结果。
     *
     * @param otelDecorator 用于完成本次业务处理的 otelDecorator 参数。
     * @return 返回 Threading Auto 相关操作生成的结果数据。
     */
    @Bean
    public TaskDecorator threadTaskDecorator(ObjectProvider<OtelTaskDecorator> otelDecorator) {
        List<TaskDecorator> decorators = new ArrayList<>();
        decorators.add(new ContextTaskDecorator());
        otelDecorator.ifAvailable(decorators::add);
        return new CompositeTaskDecorator(decorators);
    }

    /**
     * 执行 Threading Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param shutdownAll 用于完成本次业务处理的 shutdownAll 参数。
     */
    @Bean(destroyMethod = "shutdownAll")
    @ConditionalOnMissingBean(ThreadPoolManager.class)
    public ThreadPoolManager threadPoolManager(
            ThreadingProperties properties,
            @Qualifier("threadTaskDecorator") TaskDecorator taskDecorator) {
        logger.info("Creating managed worker thread pool manager");
        return new DefaultThreadPoolManager(properties, taskDecorator);
   }

    /**
     * 执行 Threading Auto 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
     */
    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnProperty(
            prefix = "shiyu.thread",
            name = "metrics-enabled",
            havingValue = "true",
            matchIfMissing = false)
   public SmartLifecycle threadPoolMetricsLifecycle(
           ThreadPoolManager threadPoolManager,
            MeterRegistry meterRegistry,
            ThreadingProperties properties) {
        return new SmartLifecycle() {
            private volatile boolean running;

            /**
             * {@code start} 执行当前类型定义的业务操作。
             */
            @Override
            public void start() {
                properties
                        .getPools()
                        .forEach(
                                (name, ignored) -> {
                                    new MicrometerExecutorBinder(
                                                    threadPoolManager.getExecutor(name), name)
                                            .bindTo(meterRegistry);
                                });
                running = true;
                logger.info("Managed worker thread pool metrics enabled");
            }

            /**
             * {@code stop} 执行当前类型定义的业务操作。
             */
            @Override
            public void stop() {
                running = false;
            }

            /**
             * {@code isRunning} 校验当前操作的输入或状态是否满足约束。
             *
             * @return 返回当前操作产生的结果。
             */
            @Override
            public boolean isRunning() {
                return running;
            }
        };
   }

   /**
    * 执行 Threading Auto 相关业务操作，并维护必要的状态和协作关系。
    *
    * @param class 用于完成本次业务处理的 class 参数。
    */
   @Bean
   @ConditionalOnClass(Tracer.class)
    @ConditionalOnBean(OpenTelemetry.class)
    @ConditionalOnProperty(
            prefix = "shiyu.thread",
            name = "otel-enabled",
           havingValue = "true",
           matchIfMissing = false)
   public OtelTaskDecorator otelTaskDecorator(OpenTelemetry openTelemetry) {
       logger.info("OpenTelemetry propagation enabled for managed worker threads");
        return new OtelTaskDecorator(openTelemetry.getTracer("shiyu-threading", "1.0.0"));
    }
}

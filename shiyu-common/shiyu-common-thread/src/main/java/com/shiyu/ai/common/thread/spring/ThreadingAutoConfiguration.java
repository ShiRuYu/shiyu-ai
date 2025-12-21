
package com.shiyu.ai.common.thread.spring;

import com.shiyu.ai.common.core.factory.YmlPropertySourceFactory;
import com.shiyu.ai.common.thread.api.ThreadPoolManager;
import com.shiyu.ai.common.thread.core.DefaultThreadPoolManager;
import com.shiyu.ai.common.thread.metrics.MicrometerExecutorBinder;
import com.shiyu.ai.common.thread.otel.OtelTaskDecorator;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.Executor;

/**
 * 线程模块自动配置类
 * 提供线程池管理器、任务装饰器等Bean的自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(ThreadingProperties.class)
@PropertySource(value = "classpath:application-thread-default.yml", factory = YmlPropertySourceFactory.class)
public class ThreadingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ThreadingAutoConfiguration.class);

    /**
     * 创建线程池管理器
     * 
     * @param properties 线程配置属性
     * @return 线程池管理器
     */
    @Bean
    @ConditionalOnMissingBean(ThreadPoolManager.class)
    public ThreadPoolManager threadPoolManager(ThreadingProperties properties,OtelTaskDecorator otelTaskDecorator) {
        logger.info("创建默认线程池管理器");
        return new DefaultThreadPoolManager(otelTaskDecorator);
    }

    /**
     * 创建带指标收集的线程池管理器
     * 
     * @param threadPoolManager 线程池管理器
     * @param meterRegistry 指标注册表
     * @param properties 线程配置属性
     * @return 线程池管理器
     */
    @Bean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(prefix = "shiyu.thread", name = "metrics-enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolManager metricsThreadPoolManager(
            ThreadPoolManager threadPoolManager,
            MeterRegistry meterRegistry,
            ThreadingProperties properties) {

        logger.info("启用线程池指标收集");

        // 为所有线程池注册指标
        properties.getPools().forEach((name, poolProps) -> {
            Executor executor = threadPoolManager.getExecutor(poolProps.getType(), name);
            MicrometerExecutorBinder binder = new MicrometerExecutorBinder(executor, name);
            binder.bindTo(meterRegistry);
        });

        return threadPoolManager;
    }

    /**
     * 创建OpenTelemetry任务装饰器
     * 
     * @param openTelemetry OpenTelemetry实例
     * @return 任务装饰器
     */
    @Bean
    @ConditionalOnClass(Tracer.class)
    @ConditionalOnProperty(prefix = "shiyu.thread", name = "otel-enabled", havingValue = "true", matchIfMissing = false)
    public OtelTaskDecorator otelTaskDecorator(OpenTelemetry openTelemetry) {
        logger.info("启用OpenTelemetry任务装饰器");
        Tracer tracer = openTelemetry.getTracer("shiyu-threading", "1.0.0");
        return new OtelTaskDecorator(tracer);
    }
}

package com.shiyu.ai.common.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.Set;

/**
 * 定义 事件 Infrastructure 基础设施或应用能力的配置项及装配规则。
 */
@ConfigurationProperties(prefix = "shiyu.infrastructure.event")
@Getter
@Setter
public class EventInfrastructureProperties {

    /**
     * 提供者，表示当前对象中的对应属性。
     */
    private String provider = "in-process";
    /**
     * topic 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String topic = "shiyu.domain-events.v1";
    /**
     * bootstrapServers 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String bootstrapServers = "127.0.0.1:9092";
    /**
     * deadLetterTopic 属性，保存当前对象中的业务数据或协作依赖。
     */
    private String deadLetterTopic = "shiyu.domain-events.v1.dlq";
    /**
     * relayIntervalMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private long relayIntervalMs = 5000L;
    /**
     * relayBatchSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int relayBatchSize = 100;
    /**
     * maxAttempts 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int maxAttempts = 5;

    /**
     * 执行 事件 Infrastructure 相关业务数据，并返回处理结果。
     *
     * @return 返回 事件 Infrastructure 相关操作生成的结果数据。
     */
    public String normalizedProvider() {
        return provider == null || provider.isBlank()
                ? "in-process"
                : provider.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * {@code validate} 校验当前操作的输入或状态是否满足约束。
     */
    public void validate() {
        Set<String> supported = Set.of("in-process", "postgres-outbox", "kafka");
        if (!supported.contains(normalizedProvider())) {
            throw new IllegalArgumentException(
                    "不支持的事件 provider: " + provider + "，可用类型: " + supported);
        }
        if (relayIntervalMs <= 0 || relayBatchSize <= 0 || maxAttempts <= 0) {
            throw new IllegalArgumentException("事件 relay 参数必须为正数");
        }
        if ("kafka".equals(normalizedProvider())
                && (topic == null
                        || topic.isBlank()
                        || bootstrapServers == null
                        || bootstrapServers.isBlank()
                        || deadLetterTopic == null
                        || deadLetterTopic.isBlank())) {
            throw new IllegalArgumentException(
                    "Kafka provider requires topic, bootstrap-servers and dead-letter-topic");
        }
    }
}

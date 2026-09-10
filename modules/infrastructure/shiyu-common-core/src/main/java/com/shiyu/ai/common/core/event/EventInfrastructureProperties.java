package com.shiyu.ai.common.core.event;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Set;

/** Selects the in-process, outbox, or Kafka event transport. */
@ConfigurationProperties(prefix = "shiyu.infrastructure.event")
public class EventInfrastructureProperties {

    private String provider = "in-process";
    private String topic = "shiyu.domain-events.v1";
    private String bootstrapServers = "127.0.0.1:9092";
    private String deadLetterTopic = "shiyu.domain-events.v1.dlq";
    private long relayIntervalMs = 5000L;
    private int relayBatchSize = 100;
    private int maxAttempts = 5;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(String bootstrapServers) { this.bootstrapServers = bootstrapServers; }
    public String getDeadLetterTopic() { return deadLetterTopic; }
    public void setDeadLetterTopic(String deadLetterTopic) { this.deadLetterTopic = deadLetterTopic; }
    public long getRelayIntervalMs() { return relayIntervalMs; }
    public void setRelayIntervalMs(long relayIntervalMs) { this.relayIntervalMs = relayIntervalMs; }
    public int getRelayBatchSize() { return relayBatchSize; }
    public void setRelayBatchSize(int relayBatchSize) { this.relayBatchSize = relayBatchSize; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public String normalizedProvider() {
        return provider == null || provider.isBlank() ? "in-process" : provider.trim().toLowerCase(Locale.ROOT);
    }

    public void validate() {
        Set<String> supported = Set.of("in-process", "postgres-outbox", "kafka");
        if (!supported.contains(normalizedProvider())) {
            throw new IllegalArgumentException("不支持的事件 provider: " + provider + "，可用类型: " + supported);
        }
        if (relayIntervalMs <= 0 || relayBatchSize <= 0 || maxAttempts <= 0) {
            throw new IllegalArgumentException("事件 relay 参数必须为正数");
        }
        if ("kafka".equals(normalizedProvider())
                && (topic == null || topic.isBlank() || bootstrapServers == null || bootstrapServers.isBlank()
                || deadLetterTopic == null || deadLetterTopic.isBlank())) {
            throw new IllegalArgumentException("Kafka provider requires topic, bootstrap-servers and dead-letter-topic");
        }
    }
}

package com.shiyu.ai.common.core.event;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/** Selects exactly one event transport without changing domain callers. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EventInfrastructureProperties.class)
public class EventInfrastructureConfiguration {

    @Bean
    public EventProviderValidator eventProviderValidator(EventInfrastructureProperties properties) {
        properties.validate();
        return new EventProviderValidator(properties.normalizedProvider());
    }

    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "in-process",
            matchIfMissing = true)
    public InfrastructureEventPublisher inProcessEventPublisher(
            ApplicationEventPublisher publisher) {
        return new InProcessEventPublisher(publisher);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "postgres-outbox")
    public InfrastructureEventPublisher jdbcOutboxEventPublisher(
            JdbcTemplate jdbc, EventInfrastructureProperties properties) {
        return new JdbcOutboxEventPublisher(jdbc, properties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "kafka")
    public ProducerFactory<String, String> eventProducerFactory(
            EventInfrastructureProperties properties) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "kafka")
    public KafkaTemplate<String, String> eventKafkaTemplate(
            ProducerFactory<String, String> factory) {
        return new KafkaTemplate<>(factory);
    }

    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "kafka")
    public InfrastructureEventPublisher kafkaEventPublisher(
            JdbcTemplate jdbc,
            EventInfrastructureProperties properties,
            KafkaTemplate<String, String> kafka) {
        return new KafkaOutboxEventPublisher(jdbc, properties, kafka);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "kafka")
    public EventConsumptionDeduplicator eventConsumptionDeduplicator(JdbcTemplate jdbc) {
        return new EventConsumptionDeduplicator(jdbc);
    }

    public record EventProviderValidator(String provider) {}
}

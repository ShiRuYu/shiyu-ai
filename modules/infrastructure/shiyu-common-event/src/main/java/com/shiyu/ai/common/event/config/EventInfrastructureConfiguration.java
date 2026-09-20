package com.shiyu.ai.common.event.config;

import com.shiyu.ai.common.event.api.InfrastructureEventPublisher;
import com.shiyu.ai.common.event.outbox.JdbcOutboxEventPublisher;
import com.shiyu.ai.common.event.outbox.KafkaOutboxEventPublisher;
import com.shiyu.ai.common.event.publisher.InProcessEventPublisher;
import com.shiyu.ai.common.event.support.EventConsumptionDeduplicator;

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

/**
 * 定义 事件 Infrastructure 基础设施或应用能力的配置项及装配规则。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EventInfrastructureProperties.class)
public class EventInfrastructureConfiguration {

    /**
     * 执行 事件 Infrastructure 相关业务数据，并返回处理结果。
     *
     * @param properties 用于完成本次业务处理的 properties 参数。
     * @return 返回 事件 Infrastructure 相关操作生成的结果数据。
     */
    @Bean
    public EventProviderValidator eventProviderValidator(EventInfrastructureProperties properties) {
        properties.validate();
        return new EventProviderValidator(properties.normalizedProvider());
   }

   /**
    * 执行 事件 Infrastructure 相关业务操作，并维护必要的状态和协作关系。
    *
    * @param event 本次流程携带的事件或业务数据。
    * @param provider 用于完成本次业务处理的 provider 参数。
    * @param process 用于完成本次业务处理的 process 参数。
    * @param true 用于完成本次业务处理的 true 参数。
    */
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

    /**
     * 执行 事件 Infrastructure 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param outbox 用于完成本次业务处理的 outbox 参数。
     */
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

    /**
     * 执行 事件 Infrastructure 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param kafka 用于完成本次业务处理的 kafka 参数。
     */
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

    /**
     * 执行 事件 Infrastructure 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param kafka 用于完成本次业务处理的 kafka 参数。
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "kafka")
   public KafkaTemplate<String, String> eventKafkaTemplate(
           ProducerFactory<String, String> factory) {
        return new KafkaTemplate<>(factory);
   }

    /**
     * 执行 事件 Infrastructure 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param kafka 用于完成本次业务处理的 kafka 参数。
     */
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

    /**
     * 执行 事件 Infrastructure 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     * @param provider 用于完成本次业务处理的 provider 参数。
     * @param kafka 用于完成本次业务处理的 kafka 参数。
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "shiyu.infrastructure.event",
            name = "provider",
            havingValue = "kafka")
   public EventConsumptionDeduplicator eventConsumptionDeduplicator(JdbcTemplate jdbc) {
       return new EventConsumptionDeduplicator(jdbc);
    }

    /**
     * 封装 事件 Provider 相关的不可变数据及其字段约束。
     */
    public record EventProviderValidator(String provider) {}
}

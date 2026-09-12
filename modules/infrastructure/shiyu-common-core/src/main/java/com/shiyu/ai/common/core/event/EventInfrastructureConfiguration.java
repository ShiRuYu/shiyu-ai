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

/**
 * EventInfrastructureConfiguration 配置组件，负责注册和配置基础设施领域相关基础设施。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(EventInfrastructureProperties.class)
public class EventInfrastructureConfiguration {

    /**
     * {@code eventProviderValidator} 执行当前类型定义的业务操作。
     *
     * @param properties 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    public EventProviderValidator eventProviderValidator(EventInfrastructureProperties properties) {
        properties.validate();
        return new EventProviderValidator(properties.normalizedProvider());
   }

    /**
     * {@code inProcessEventPublisher} 执行当前类型定义的业务操作。
     *
     * @param publisher 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
    * {@code jdbcOutboxEventPublisher} 执行当前类型定义的业务操作。
     *
     * @param jdbc 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
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
    * {@code eventProducerFactory} 执行当前类型定义的业务操作。
     *
     * @param properties 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
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
    * {@code eventKafkaTemplate} 执行当前类型定义的业务操作。
     *
     * @param factory 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
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
    * {@code kafkaEventPublisher} 执行当前类型定义的业务操作。
     *
     * @param jdbc 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     * @param kafka 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
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
    * {@code eventConsumptionDeduplicator} 执行当前类型定义的业务操作。
     *
     * @param jdbc 参数值，用于执行当前操作。
     *
    * @return 返回当前操作产生的结果。
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
     * {@code EventProviderValidator} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param provider 提供方，表示该记录组件承载的数据。
     */
    public record EventProviderValidator(String provider) {}
}

package com.shiyu.ai.common.core.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/** Verifies the optional Kafka provider against a real broker when Docker is available. */
@Testcontainers(disabledWithoutDocker = true)
class KafkaContainerSmokeTest {

    @Container
    static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Test
    void publishesAndConsumesAnEvent() {
        String topic = "shiyu-test-" + UUID.randomUUID();
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties());
                KafkaConsumer<String, String> consumer =
                        new KafkaConsumer<>(consumerProperties())) {
            consumer.subscribe(List.of(topic));
            producer.send(
                    new org.apache.kafka.clients.producer.ProducerRecord<>(
                            topic, "tenant-1", "event-1"));
            producer.flush();

            long deadline = System.nanoTime() + Duration.ofSeconds(15).toNanos();
            String payload = null;
            while (payload == null && System.nanoTime() < deadline) {
                for (var record : consumer.poll(Duration.ofMillis(500))) {
                    payload = record.value();
                    break;
                }
            }
            assertThat(payload).isEqualTo("event-1");
        }
    }

    private static Properties producerProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        return properties;
    }

    private static Properties consumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "shiyu-test-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return properties;
    }
}

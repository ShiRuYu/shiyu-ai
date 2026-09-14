package com.shiyu.ai.common.core.event;

import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 将 outbox 事件发布到 Kafka 并保留失败重试能力。
 */
public final class KafkaOutboxEventPublisher extends JdbcOutboxEventPublisher {

    /**
     * kafka 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KafkaTemplate<String, String> kafka;

    /**
     * {@code KafkaOutboxEventPublisher} 创建并初始化当前类型实例。
     *
     * @param jdbc 参数值，用于执行当前操作。
     * @param properties 参数值，用于执行当前操作。
     * @param kafka 参数值，用于执行当前操作。
     */
    public KafkaOutboxEventPublisher(
            JdbcTemplate jdbc,
            EventInfrastructureProperties properties,
            KafkaTemplate<String, String> kafka) {
        super(jdbc, properties);
        this.kafka = kafka;
    }

    /**
     * {@code publish} 执行当前模块定义的业务流程。
     *
     * @param event 参数值，用于执行当前操作。
     */
    @Override
    public void publish(DomainEventEnvelope<?> event) {
        super.publish(event);
        relayPending();
    }

    /**
     * {@code relayPending} 执行当前类型定义的业务操作。
     */
    @Scheduled(fixedDelayString = "${shiyu.infrastructure.event.relay-interval-ms:5000}")
    public void relayPending() {
        int batchSize = Math.max(1, properties.getRelayBatchSize());
        List<Map<String, Object>> pending =
                jdbc.queryForList(
                        """
                        SELECT event_id, tenant_id, payload, attempts
                        FROM shiyu_event_outbox
                        WHERE published_at IS NULL AND dead_lettered_at IS NULL
                        ORDER BY created_at
                        LIMIT ?
                        """,
                        batchSize);
        for (Map<String, Object> record : pending) {
            String eventId = String.valueOf(record.get("event_id"));
            String key = String.valueOf(record.get("tenant_id"));
            String payload = String.valueOf(record.get("payload"));
            int attempts = record.get("attempts") instanceof Number number ? number.intValue() : 0;
            int nextAttempt = attempts + 1;
            jdbc.update(
                    "UPDATE shiyu_event_outbox SET attempts = attempts + 1 WHERE event_id = ?",
                    eventId);
            kafka.send(properties.getTopic(), key, payload)
                    .whenComplete(
                            (result, error) -> {
                                if (error == null) {
                                    jdbc.update(
                                            "UPDATE shiyu_event_outbox SET published_at = ? WHERE"
                                                    + " event_id = ?",
                                            Timestamp.from(Instant.now()),
                                            eventId);
                                } else {
                                    jdbc.update(
                                            "UPDATE shiyu_event_outbox SET last_error = ? WHERE"
                                                    + " event_id = ?",
                                            error.getClass().getSimpleName(),
                                            eventId);
                                    if (nextAttempt >= properties.getMaxAttempts()) {
                                        kafka.send(properties.getDeadLetterTopic(), key, payload)
                                                .whenComplete(
                                                        (deadLetterResult, deadLetterError) -> {
                                                            if (deadLetterError == null) {
                                                                jdbc.update(
                                                                        "UPDATE shiyu_event_outbox"
                                                                            + " SET dead_lettered_at"
                                                                            + " = ? WHERE event_id"
                                                                            + " = ?",
                                                                        Timestamp.from(
                                                                                Instant.now()),
                                                                        eventId);
                                                            } else {
                                                                jdbc.update(
                                                                        "UPDATE shiyu_event_outbox"
                                                                            + " SET last_error = ?"
                                                                            + " WHERE event_id = ?",
                                                                        deadLetterError
                                                                                .getClass()
                                                                                .getSimpleName(),
                                                                        eventId);
                                                            }
                                                        });
                                    }
                                }
                            });
        }
    }
}

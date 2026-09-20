package com.shiyu.ai.common.event.outbox;

import com.shiyu.ai.common.event.config.EventInfrastructureProperties;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 发布 Kafka Outbox 事件 相关的领域事件或基础设施消息。
 */
public final class KafkaOutboxEventPublisher extends JdbcOutboxEventPublisher {

    /**
     * kafka 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final KafkaTemplate<String, String> kafka;

    /**
     * 执行 Kafka Outbox 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
     * @param properties 用于完成本次业务处理的 properties 参数。
     * @param kafka 用于完成本次业务处理的 kafka 参数。
     */
    public KafkaOutboxEventPublisher(
            JdbcTemplate jdbc,
            EventInfrastructureProperties properties,
            KafkaTemplate<String, String> kafka) {
        super(jdbc, properties);
        this.kafka = kafka;
    }

    /**
     * 发布或发送 Kafka Outbox 事件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
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

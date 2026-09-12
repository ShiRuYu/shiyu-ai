package com.shiyu.ai.common.core.event;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

/**
 * 记录已消费事件并阻止同一事件重复处理。
 */
public final class EventConsumptionDeduplicator {

    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * {@code EventConsumptionDeduplicator} 创建并初始化当前类型实例。
     *
     * @param jdbc 参数值，用于执行当前操作。
     */
    public EventConsumptionDeduplicator(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc, "JdbcTemplate must not be null");
        jdbc.execute(
                """
                CREATE TABLE IF NOT EXISTS shiyu_event_inbox (
                    event_id VARCHAR(64) PRIMARY KEY,
                    consumed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    /**
     * {@code firstSeen} 执行当前类型定义的业务操作。
     *
     * @param eventId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean firstSeen(String eventId) {
        if (eventId == null || eventId.isBlank()) return false;
        try {
            return jdbc.update(
                            "INSERT INTO shiyu_event_inbox (event_id, consumed_at) VALUES (?,"
                                    + " CURRENT_TIMESTAMP)",
                            eventId)
                    == 1;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}

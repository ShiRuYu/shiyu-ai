package com.shiyu.ai.common.event.support;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

/**
 * 实现 事件 Consumption Deduplicator 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class EventConsumptionDeduplicator {

    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;

    /**
     * 执行 事件 Consumption Deduplicator 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param jdbc 用于完成本次业务处理的 jdbc 参数。
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
     * 执行 事件 Consumption Deduplicator 相关业务数据，并返回处理结果。
     *
     * @param eventId 用于定位event的标识。
     * @return 返回本次条件判断是否成立。
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

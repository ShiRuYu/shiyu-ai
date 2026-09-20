package com.shiyu.ai.common.event.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 验证 事件 Consumption Deduplicator 相关功能、边界条件、异常路径和协作行为。
 */
class EventConsumptionDeduplicatorTest {

    @Test
    void acceptsAnEventOnlyOnce() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:event-inbox;DB_CLOSE_DELAY=-1");
        EventConsumptionDeduplicator deduplicator =
                new EventConsumptionDeduplicator(new JdbcTemplate(dataSource));

        assertThat(deduplicator.firstSeen("event-1")).isTrue();
        assertThat(deduplicator.firstSeen("event-1")).isFalse();
        assertThat(deduplicator.firstSeen(" ")).isFalse();
    }
}

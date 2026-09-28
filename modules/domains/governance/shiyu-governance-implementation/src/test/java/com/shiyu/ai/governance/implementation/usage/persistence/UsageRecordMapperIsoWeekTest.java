package com.shiyu.ai.governance.implementation.usage.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shiyu.ai.governance.implementation.usage.persistence.mapper.UsageRecordMapper;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 验证 usage 记录按 ISO 周计算和映射的边界行为。 */
class UsageRecordMapperIsoWeekTest {

    @Test
    void weekAggregationUsesIsoWeekYearAtCalendarYearBoundary() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:usage_iso_week;DB_CLOSE_DELAY=-1");
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE governance_usage_record ("
                            + "id BIGINT, usage_type VARCHAR(32), latency_ms BIGINT, "
                            + "tenant_id BIGINT, create_time TIMESTAMP)");
            statement.execute(
                    "INSERT INTO governance_usage_record"
                            + " (id, usage_type, latency_ms, tenant_id, create_time) VALUES"
                            + " (1, 'LLM', 10, 1, TIMESTAMP '2021-01-01 12:00:00'),"
                            + " (2, 'LLM', 20, 1, TIMESTAMP '2021-01-04 12:00:00')");
        }

        org.apache.ibatis.session.Configuration configuration =
                new org.apache.ibatis.session.Configuration(
                        new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.addMapper(UsageRecordMapper.class);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = sessionFactory.openSession()) {
            List<Map<String, Object>> rows =
                    session.getMapper(UsageRecordMapper.class).aggregateByWeekAllTenants(400_000);

            assertEquals(
                    Set.of("2020-53", "2021-01"),
                    rows.stream()
                            .map(row -> valueIgnoreCase(row, "usage_week"))
                            .map(String::valueOf)
                            .collect(Collectors.toSet()));
        }
    }

    private static Object valueIgnoreCase(Map<String, Object> row, String key) {
        return row.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}

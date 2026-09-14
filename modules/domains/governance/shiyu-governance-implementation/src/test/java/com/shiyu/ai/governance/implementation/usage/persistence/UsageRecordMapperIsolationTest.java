package com.shiyu.ai.governance.implementation.usage.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shiyu.ai.governance.implementation.usage.persistence.mapper.UsageRecordMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class UsageRecordMapperIsolationTest {

    @AfterEach
    void clearTenantScope() {
        TenantScope.clear();
    }

    @Test
    void currentTenantOverviewUsesTheExplicitTenantParameter() throws Exception {
        DataSource dataSource =
                new PooledDataSource(
                        "org.h2.Driver",
                        "jdbc:h2:mem:usage_mapper;MODE=MySQL;DB_CLOSE_DELAY=-1",
                        "sa",
                        "");
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE governance_usage_record ("
                            + "id VARCHAR(64), tenant_id BIGINT, usage_type VARCHAR(32), "
                            + "latency_ms BIGINT, user_id BIGINT, session_id VARCHAR(128), "
                            + "ext_info VARCHAR(2000), create_time TIMESTAMP)");
            statement.execute(
                    "INSERT INTO governance_usage_record VALUES "
                            + "('one', 1, 'LLM', 10, 7, NULL, NULL, CURRENT_TIMESTAMP),"
                            + "('two', 2, 'LLM', 20, 8, NULL, NULL, CURRENT_TIMESTAMP)");
        }

        Configuration configuration =
                new Configuration(
                        new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.addMapper(UsageRecordMapper.class);
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        TenantScope.set(new TenantId(1L));
        try (var session = sessionFactory.openSession()) {
            Map<String, Object> overview = session.getMapper(UsageRecordMapper.class).getOverview(1L);
            assertEquals(1L, ((Number) overview.get("TOTAL_CALLS")).longValue());
        }
    }
}

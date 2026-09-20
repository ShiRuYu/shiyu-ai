package com.shiyu.ai.common.mybatis;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.core.mybatis.FlexSqlSessionFactoryBuilder;
import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.common.mybatis.tenant.ContextTenantFactory;
import com.shiyu.ai.common.mybatis.config.MybatisConfig;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 租户 Mapper Isolation 相关功能、边界条件、异常路径和协作行为。
 */
class TenantMapperIsolationTest {
    /**
     * 验证 Probe 相关功能、边界条件、异常路径和协作行为。
     */
    @Table("tenant_isolation_probe")

    public static class Probe {
        @Id private Long id;
        @Column(tenantId = true) private Long tenantId;
        private String label;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    /**
     * 验证 Probe 相关功能、边界条件、异常路径和协作行为。
     */
    public interface ProbeMapper extends BaseMapper<Probe> {}

    @Test
    void realMapperEnforcesScopeAndEntityConsistency() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:tenant_probe_" + java.util.UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE tenant_isolation_probe(id BIGINT PRIMARY KEY, tenant_id BIGINT NOT NULL, label VARCHAR(80))");
        var configuration = new FlexConfiguration(new Environment("tenant-probe", new JdbcTransactionFactory(),
                new com.mybatisflex.core.datasource.FlexDataSource("tenant-probe", dataSource)));
        var factory = new FlexSqlSessionFactoryBuilder().build(configuration);
        new MybatisConfig().mybatisFlexCustomizer().customize(FlexGlobalConfig.getDefaultConfig());
        configuration.addMapper(ProbeMapper.class);
        var previousFactory = TenantManager.getTenantFactory();
        TenantManager.setTenantFactory(new ContextTenantFactory());
        try (var session = factory.openSession(true)) {
            var mapper = session.getMapper(ProbeMapper.class);
            TenantScope.clear();
            assertThrows(RuntimeException.class, () -> mapper.insert(probe(1, 1L)));
            assertEquals(0L, jdbc.queryForObject("SELECT COUNT(*) FROM tenant_isolation_probe", Long.class));
            TenantScope.set(new TenantId(1));
            mapper.insert(probe(1, null));
            assertEquals(1L, jdbc.queryForObject("SELECT tenant_id FROM tenant_isolation_probe WHERE id=1", Long.class));
            assertThrows(RuntimeException.class, () -> mapper.insert(probe(2, 2L)));
            assertThrows(RuntimeException.class,
                    () -> mapper.insertBatch(java.util.List.of(probe(3, 1L), probe(4, 2L))));
            assertEquals(1L, jdbc.queryForObject("SELECT COUNT(*) FROM tenant_isolation_probe", Long.class));
            TenantScope.set(new TenantId(2));
            mapper.insert(probe(2, 2L));
            var page = mapper.paginate(1, 10, com.mybatisflex.core.query.QueryWrapper.create());
            assertEquals(1, page.getRecords().size());
            assertEquals(2L, page.getRecords().getFirst().getTenantId());
            assertNull(mapper.selectOneById(1L));
            assertEquals(0, mapper.update(probe(1, 2L)));
            assertEquals(0, mapper.deleteById(1L));
            assertThrows(RuntimeException.class, () -> mapper.update(probe(2, 1L)));
            assertEquals(1L, jdbc.queryForObject("SELECT tenant_id FROM tenant_isolation_probe WHERE id=1", Long.class));
            assertEquals(2L, jdbc.queryForObject("SELECT tenant_id FROM tenant_isolation_probe WHERE id=2", Long.class));
            assertEquals(1, mapper.update(probe(2, null)));
            assertEquals(1, mapper.deleteById(2L));
            assertEquals(1L, jdbc.queryForObject("SELECT COUNT(*) FROM tenant_isolation_probe", Long.class));
            TenantScope.clear();
            assertThrows(RuntimeException.class, () -> mapper.selectOneById(1L));
        } finally {
            TenantScope.clear();
            TenantManager.setTenantFactory(previousFactory);
            jdbc.execute("DROP TABLE tenant_isolation_probe");
        }
    }

    private static Probe probe(long id, Long tenantId) {
        var probe = new Probe();
        probe.setId(id);
        probe.setTenantId(tenantId);
        probe.setLabel("probe");
        return probe;
    }
}

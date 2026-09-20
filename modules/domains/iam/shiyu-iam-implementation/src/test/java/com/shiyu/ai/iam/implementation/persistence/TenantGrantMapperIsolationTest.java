package com.shiyu.ai.iam.implementation.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static com.mybatisflex.core.query.QueryMethods.column;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.datasource.FlexDataSource;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.core.mybatis.FlexSqlSessionFactoryBuilder;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.common.mybatis.tenant.ContextTenantFactory;
import com.shiyu.ai.common.mybatis.config.MybatisConfig;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantMenuDO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantAuthCodeDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.TenantAuthCodeMapper;
import com.shiyu.ai.iam.implementation.persistence.mapper.TenantMenuMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 验证 租户 Grant Mapper Isolation 相关功能、边界条件、异常路径和协作行为。
 */
class TenantGrantMapperIsolationTest {
    @Test
    void realGrantMappersFilterQueriesAndRejectCrossTenantWrites() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:grant_" + java.util.UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE auth_tenant_menu(tenant_id BIGINT, menu_id BIGINT, status INT)");
        jdbc.execute("CREATE TABLE auth_tenant_auth_code(tenant_id BIGINT, auth_code_id BIGINT, status INT)");
        jdbc.update("INSERT INTO auth_tenant_menu VALUES (1,10,1),(2,20,1)");
        jdbc.update("INSERT INTO auth_tenant_auth_code VALUES (1,10,1),(2,10,1)");
        var configuration = new FlexConfiguration(new Environment("agent", new JdbcTransactionFactory(),
                new FlexDataSource("agent", dataSource)));
        var factory = new FlexSqlSessionFactoryBuilder().build(configuration);
        new MybatisConfig().mybatisFlexCustomizer().customize(FlexGlobalConfig.getDefaultConfig());
        configuration.addMapper(TenantMenuMapper.class);
        configuration.addMapper(TenantAuthCodeMapper.class);
        var previousFactory = TenantManager.getTenantFactory();
        var previousScope = TenantScope.current().orElse(null);
        TenantManager.setTenantFactory(new ContextTenantFactory());
        try (var session = factory.openSession(true)) {
            var menus = session.getMapper(TenantMenuMapper.class);
            var codes = session.getMapper(TenantAuthCodeMapper.class);
            TenantScope.set(new TenantId(1));
            assertEquals(1, menus.selectListByQuery(QueryWrapper.create()).size());
            assertEquals(1, codes.selectListByQuery(QueryWrapper.create()).size());
            assertEquals(1L, codes.selectListByQuery(QueryWrapper.create()).getFirst().getTenantId());
            var joined = QueryWrapper.create().from(TenantMenuDO.class)
                    .innerJoin(TenantAuthCodeDO.class)
                    .on(column(TenantMenuDO::getMenuId).eq(column(TenantAuthCodeDO::getAuthCodeId)));
            assertEquals(1L, menus.selectCountByQuery(joined));
            assertEquals(0, menus.deleteByQuery(QueryWrapper.create().eq("menu_id", 20L)));
            var forbidden = new TenantMenuDO();
            forbidden.setTenantId(2L);
            forbidden.setMenuId(30L);
            forbidden.setStatus(1);
            assertThrows(RuntimeException.class, () -> menus.insert(forbidden));
            assertEquals(2L, jdbc.queryForObject("SELECT COUNT(*) FROM auth_tenant_menu", Long.class));
            TenantScope.clear();
            assertThrows(RuntimeException.class, () -> menus.selectListByQuery(QueryWrapper.create()));
            assertThrows(RuntimeException.class, () -> codes.selectListByQuery(QueryWrapper.create()));
        } finally {
            TenantManager.setTenantFactory(previousFactory);
            if (previousScope == null) TenantScope.clear();
            else TenantScope.set(previousScope);
            jdbc.execute("DROP TABLE auth_tenant_menu");
            jdbc.execute("DROP TABLE auth_tenant_auth_code");
        }
    }
}

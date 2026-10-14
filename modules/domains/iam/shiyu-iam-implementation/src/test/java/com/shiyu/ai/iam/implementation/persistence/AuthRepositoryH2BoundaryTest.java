package com.shiyu.ai.iam.implementation.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.datasource.FlexDataSource;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.core.mybatis.FlexSqlSessionFactoryBuilder;
import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.common.mybatis.config.MybatisConfig;
import com.shiyu.ai.common.mybatis.tenant.ContextTenantFactory;
import com.shiyu.ai.iam.implementation.persistence.mapper.AuthCodeMapper;
import com.shiyu.ai.iam.implementation.persistence.mapper.RoleMapper;
import com.shiyu.ai.iam.implementation.persistence.repository.AuthRepositoryImpl;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/** 验证真实 SQL 不会从历史停用角色或跨租户错绑授权中返回权限。 */
class AuthRepositoryH2BoundaryTest {
    @Test
    void permissionQueriesRejectStaleAndCrossTenantRoleGrants() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:auth_boundary_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE auth_user(id BIGINT, username VARCHAR(80), status INT, del_flag INT)");
        jdbc.execute("CREATE TABLE auth_role(id BIGINT, tenant_id BIGINT, code VARCHAR(80), status INT, del_flag INT)");
        jdbc.execute("CREATE TABLE auth_user_scope_role(user_id BIGINT, role_id BIGINT, tenant_id BIGINT, status INT, del_flag INT)");
        jdbc.execute("CREATE TABLE auth_role_scope_auth_code(role_id BIGINT, auth_code_id BIGINT, tenant_id BIGINT, status INT, del_flag INT)");
        jdbc.execute("CREATE TABLE auth_auth_code(id BIGINT, code VARCHAR(80), status INT, del_flag INT)");
        jdbc.update("INSERT INTO auth_user VALUES (1, 'alice', 1, 0)");
        jdbc.update("INSERT INTO auth_role VALUES (10, 1, 'member', 1, 0),"
                + "(11, 1, 'old_admin', 0, 0),(20, 2, 'foreign_admin', 1, 0)");
        jdbc.update("INSERT INTO auth_user_scope_role VALUES (1, 10, 1, 1, 0),"
                + "(1, 11, 1, 1, 0),(1, 20, 1, 1, 0)");
        jdbc.update("INSERT INTO auth_role_scope_auth_code VALUES (10, 100, 1, 1, 0),"
                + "(11, 101, 1, 1, 0),(20, 102, 1, 1, 0),(10, 103, 2, 1, 0)");
        jdbc.update("INSERT INTO auth_auth_code VALUES (100, 'allowed', 1, 0),"
                + "(101, 'stale', 1, 0),(102, 'foreign', 1, 0),(103, 'other_tenant', 1, 0)");

        var configuration = new FlexConfiguration(new Environment(
                "agent", new JdbcTransactionFactory(), new FlexDataSource("agent", dataSource)));
        var factory = new FlexSqlSessionFactoryBuilder().build(configuration);
        new MybatisConfig().mybatisFlexCustomizer().customize(FlexGlobalConfig.getDefaultConfig());
        configuration.addMapper(AuthCodeMapper.class);
        configuration.addMapper(RoleMapper.class);
        var previousFactory = TenantManager.getTenantFactory();
        var previousScope = TenantScope.current().orElse(null);
        TenantManager.setTenantFactory(new ContextTenantFactory());
        try (var session = factory.openSession(true)) {
            var repository = new AuthRepositoryImpl();
            ReflectionTestUtils.setField(
                    repository, "authCodeMapper", session.getMapper(AuthCodeMapper.class));
            ReflectionTestUtils.setField(
                    repository, "roleMapper", session.getMapper(RoleMapper.class));
            TenantScope.set(new TenantId(1));

            assertEquals(List.of("member"),
                    repository.selectRoleCodesByUserId(new UserId(1), new TenantId(1)));
            assertEquals(List.of("allowed"),
                    repository.selectCodesByUserId(new UserId(1), new TenantId(1)));
            assertEquals(List.of("allowed"),
                    repository.selectCodesByUsername("alice", new TenantId(1)));
            assertEquals(List.of("allowed"), repository.selectCodesByUserIdAndRoleCode(
                    new UserId(1), new TenantId(1), "member"));
        } finally {
            TenantManager.setTenantFactory(previousFactory);
            if (previousScope == null) {
                TenantScope.clear();
            } else {
                TenantScope.set(previousScope);
            }
        }
    }
}

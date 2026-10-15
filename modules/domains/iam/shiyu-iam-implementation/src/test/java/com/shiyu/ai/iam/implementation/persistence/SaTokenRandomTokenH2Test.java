package com.shiyu.ai.iam.implementation.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import cn.dev33.satoken.session.SaSession;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.datasource.FlexDataSource;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.core.mybatis.FlexSqlSessionFactoryBuilder;
import com.shiyu.ai.common.mybatis.config.MybatisConfig;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.infrastructure.auth.SaTokenDaoImpl;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.UserMapper;
import com.shiyu.ai.iam.implementation.persistence.repository.SaTokenUserRepositoryImpl;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

/** 验证数据库能够准确查找已持久化随机令牌的所属用户。 */
class SaTokenRandomTokenH2Test {
    @Test
    void persistedRandomTokenResolvesWithoutAnInMemoryReverseIndex() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:sa_token_restart_" + UUID.randomUUID()
                + ";DB_CLOSE_DELAY=-1");
        var jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE auth_user (id BIGINT PRIMARY KEY, username VARCHAR(80),"
                + " password VARCHAR(255), status INT, del_flag INT, create_by VARCHAR(64),"
                + " create_time TIMESTAMP, update_by VARCHAR(64), update_time TIMESTAMP,"
                + " nick_name VARCHAR(80), gender VARCHAR(10), avatar VARCHAR(255),"
                + " address VARCHAR(255), email VARCHAR(128), phone VARCHAR(20),"
                + " remark VARCHAR(500), ext_info VARCHAR)");
        String opaqueToken = "bcf38261-60f9-4790-83bb-a5e33e917d17";
        jdbc.update("INSERT INTO auth_user(id, username, status, del_flag, ext_info)"
                + " VALUES (40, 'damaged', 1, 0, ?)",
                "{\"tokens\":{\"" + opaqueToken + "\":");
        jdbc.update("INSERT INTO auth_user(id, username, status, del_flag, ext_info)"
                + " VALUES (41, 'similar', 1, 0, ?)",
                "{\"tokens\":{\"" + opaqueToken + "-other\":{\"loginId\":\"41\"}}}");
        jdbc.update("INSERT INTO auth_user(id, username, status, del_flag, ext_info)"
                + " VALUES (42, 'member', 1, 0, ?)",
                "{\"tokens\":{\"" + opaqueToken + "\":{\"loginId\":\"42\"}}}");

        var configuration = new FlexConfiguration(new Environment(
                "agent", new JdbcTransactionFactory(), new FlexDataSource("agent", dataSource)));
        new MybatisConfig().mybatisFlexCustomizer().customize(FlexGlobalConfig.getDefaultConfig());
        configuration.addMapper(UserMapper.class);
        var factory = new FlexSqlSessionFactoryBuilder().build(configuration);
        try (var session = factory.openSession(true)) {
            var repository = new SaTokenUserRepositoryImpl();
            ReflectionTestUtils.setField(repository, "userMapper",
                    session.getMapper(UserMapper.class));
            assertEquals(42L, repository.findUserIdByToken(opaqueToken));
            assertNull(repository.findUserIdByToken(opaqueToken + "-missing"));
            assertNull(repository.findUserIdByToken(null));
            assertNull(repository.findUserIdByToken(" "));
            assertNull(repository.findUserIdByToken("%".repeat(36)));
            assertNull(repository.findUserIdByToken("_".repeat(36)));
            assertNull(repository.findUserIdByToken("a".repeat(129)));

            try (MockedStatic<MapstructUtils> mappings = mockStatic(MapstructUtils.class)) {
                mappings.when(() -> MapstructUtils.convert(any(UserDO.class), eq(UserBO.class)))
                        .thenAnswer(call -> {
                            UserDO row = call.getArgument(0);
                            UserBO user = new UserBO();
                            user.setId(row.getId());
                            user.setExtInfo(row.getExtInfo());
                            return user;
                        });
                mappings.when(() -> MapstructUtils.convert(any(UserBO.class), eq(UserDO.class)))
                        .thenAnswer(call -> {
                            UserBO user = call.getArgument(0);
                            UserDO row = new UserDO();
                            row.setId(user.getId());
                            row.setExtInfo(user.getExtInfo());
                            return row;
                        });
                String tokenKey = "Authorization:login:token:" + opaqueToken;
                String sessionKey = "Authorization:login:token-session:" + opaqueToken;
                SaTokenDaoImpl beforeRestart = new SaTokenDaoImpl(repository);
                beforeRestart.set(tokenKey, "42", 3600);
                beforeRestart.setSession(new SaSession(sessionKey), 3600);
                beforeRestart.destroy();
                SaTokenDaoImpl afterRestart = new SaTokenDaoImpl(repository);
                try {
                    assertEquals("42", afterRestart.get(tokenKey));
                    assertTrue(afterRestart.getTimeout(tokenKey) > 0);
                    assertNotNull(afterRestart.getSession(sessionKey));
                } finally {
                    afterRestart.destroy();
                }
            }
        }
    }
}

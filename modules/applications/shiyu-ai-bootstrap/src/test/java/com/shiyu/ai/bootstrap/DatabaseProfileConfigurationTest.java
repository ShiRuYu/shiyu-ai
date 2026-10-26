package com.shiyu.ai.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/** 覆盖数据库 provider 配置与 Spring profile 选择规则。 */
class DatabaseProfileConfigurationTest {

    @TempDir Path appHome;

    @Test
    void devProfileSelectsAndConnectsToAnEmptyH2Datasource() throws SQLException {
        String previousHome = System.getProperty("app.home");
        String previousLogPath = System.getProperty("logging.file.path");
        System.setProperty("app.home", appHome.toString());
        System.setProperty("logging.file.path", appHome.resolveSibling(
                appHome.getFileName() + "-logs").toString());
        SpringApplication application = new SpringApplication(ProfileProbe.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setDefaultProperties(Map.of(
                "spring.profiles.active", "dev",
                "spring.main.banner-mode", "off"));

        try (ConfigurableApplicationContext context = application.run()) {
            assertTrue(Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("h2"));
            assertEquals("org.h2.Driver",
                    context.getEnvironment().getProperty("mybatis-flex.datasource.agent.driverClassName"));
            String h2Url = context.getEnvironment()
                    .getProperty("mybatis-flex.datasource.agent.url");
            assertTrue(h2Url.replace('\\', '/').contains(
                    appHome.resolve("data/db/shiyudb").toAbsolutePath().normalize().toString()
                            .replace('\\', '/')));
            assertTrue(Boolean.parseBoolean(
                    context.getEnvironment().getProperty("spring.h2.console.enabled")));
            Path databaseFile = appHome.resolve("data/db/shiyudb.mv.db");
            assertFalse(Files.exists(databaseFile), "the isolated H2 database must start empty");
            try (Connection connection = DriverManager.getConnection(
                    h2Url,
                    context.getEnvironment().getProperty("mybatis-flex.datasource.agent.username"),
                    context.getEnvironment().getProperty("mybatis-flex.datasource.agent.password", ""));
                    ResultSet tables = connection.getMetaData().getTables(
                            null, "PUBLIC", "%", new String[] {"TABLE"})) {
                assertTrue(connection.isValid(2));
                assertFalse(
                        tables.next(),
                        "the profile probe must not run schema or seed initialization");
            }
        } finally {
            LogManager.shutdown();
            if (previousHome == null) System.clearProperty("app.home");
            else System.setProperty("app.home", previousHome);
            if (previousLogPath == null) System.clearProperty("logging.file.path");
            else System.setProperty("logging.file.path", previousLogPath);
        }
    }

    @Test
    void kafkaEnvironmentAliasesResolveWithoutEnablingTheOptionalModule() {
        String previousHome = System.getProperty("app.home");
        String previousLogPath = System.getProperty("logging.file.path");
        System.setProperty("app.home", appHome.toString());
        System.setProperty("logging.file.path", appHome.resolveSibling(
                appHome.getFileName() + "-logs").toString());
        SpringApplication application = new SpringApplication(ProfileProbe.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setDefaultProperties(Map.of(
                "spring.profiles.active", "dev",
                "spring.main.banner-mode", "off"));
        application.addInitializers(context -> context.getEnvironment().getPropertySources().addFirst(
                new SystemEnvironmentPropertySource("testEnvironment", Map.of(
                        "SHIYU_INFRA_EVENT_PROVIDER", "kafka",
                        "SHIYU_KAFKA_TOPIC", "test.events",
                        "SHIYU_KAFKA_BOOTSTRAP_SERVERS", "127.0.0.1:19092"))));

        try (ConfigurableApplicationContext context = application.run()) {
            assertEquals("kafka", context.getEnvironment()
                    .getProperty("shiyu.infrastructure.event.provider"));
            assertEquals("test.events", context.getEnvironment()
                    .getProperty("shiyu.infrastructure.event.topic"));
            assertEquals("127.0.0.1:19092", context.getEnvironment()
                    .getProperty("shiyu.infrastructure.event.bootstrap-servers"));
        } finally {
            LogManager.shutdown();
            if (previousHome == null) System.clearProperty("app.home");
            else System.setProperty("app.home", previousHome);
            if (previousLogPath == null) System.clearProperty("logging.file.path");
            else System.setProperty("logging.file.path", previousLogPath);
        }
    }

    @Test
    void postgresqlSelectionDoesNotLetTheWindowsH2ProfileOverrideItsDatasource() {
        String previousHome = System.getProperty("app.home");
        String previousLogPath = System.getProperty("logging.file.path");
        System.setProperty("app.home", appHome.toString());
        SpringApplication application = new SpringApplication(ProfileProbe.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setDefaultProperties(Map.of(
                "spring.profiles.active", "windows",
                "spring.profiles.include", "postgresql",
                "spring.main.banner-mode", "off"));
        application.addInitializers(
                context ->
                        context.getEnvironment()
                                .getPropertySources()
                                .addFirst(
                                        new SystemEnvironmentPropertySource(
                                                "testEnvironment",
                                                Map.of(
                                                        "SHIYU_VECTOR_STORE_TYPE", "pgvector",
                                                        "mybatis-flex.datasource.agent.password",
                                                        "profile-secret"))));

        try (ConfigurableApplicationContext context = application.run()) {
            assertTrue(Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("postgresql"));
            assertEquals(
                    "org.postgresql.Driver",
                    context.getEnvironment()
                            .getProperty("mybatis-flex.datasource.agent.driver-class-name"));
            assertEquals("jdbc:postgresql://localhost:5433/postgres",
                    context.getEnvironment().getProperty("mybatis-flex.datasource.agent.url"));
            assertFalse(Boolean.parseBoolean(
                    context.getEnvironment().getProperty("shiyu.storage.backup.enabled", "true")));
            assertFalse(
                    Boolean.TRUE.equals(
                            context.getEnvironment()
                                    .getProperty("spring.h2.console.enabled", Boolean.class)));
            assertEquals(
                    "pgvector",
                    context.getEnvironment().getProperty("shiyu.vector-store.type"));
            assertEquals(
                    "profile-secret",
                    context.getEnvironment()
                            .getProperty("mybatis-flex.datasource.agent.password"));
        } finally {
            LogManager.shutdown();
            if (previousHome == null) System.clearProperty("app.home");
            else System.setProperty("app.home", previousHome);
            if (previousLogPath == null) System.clearProperty("logging.file.path");
            else System.setProperty("logging.file.path", previousLogPath);
        }
    }

    /** 暴露测试上下文中最终生效的 profile 配置。 */
    @Configuration(proxyBeanMethods = false)
    static class ProfileProbe {}
}

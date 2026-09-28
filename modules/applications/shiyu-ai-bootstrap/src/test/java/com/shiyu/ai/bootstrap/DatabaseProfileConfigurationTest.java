package com.shiyu.ai.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/** 覆盖数据库 provider 配置与 Spring profile 选择规则。 */
class DatabaseProfileConfigurationTest {

    @TempDir Path appHome;

    @Test
    void postgresqlSelectionDoesNotLetTheWindowsH2ProfileOverrideItsDatasource() {
        String previousHome = System.getProperty("app.home");
        System.setProperty("app.home", appHome.toString());
        SpringApplication application = new SpringApplication(ProfileProbe.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setDefaultProperties(Map.of(
                "spring.profiles.active", "windows",
                "spring.profiles.include", "postgresql",
                "spring.main.banner-mode", "off"));

        try (ConfigurableApplicationContext context = application.run()) {
            assertTrue(Arrays.asList(context.getEnvironment().getActiveProfiles()).contains("postgresql"));
            assertEquals("org.postgresql.Driver",
                    context.getEnvironment().getProperty("mybatis-flex.datasource.agent.driver-class-name"));
            assertFalse(Boolean.TRUE.equals(context.getEnvironment().getProperty("spring.h2.console.enabled", Boolean.class)));
        } finally {
            if (previousHome == null) System.clearProperty("app.home");
            else System.setProperty("app.home", previousHome);
        }
    }

    /** 暴露测试上下文中最终生效的 profile 配置。 */
    @Configuration(proxyBeanMethods = false)
    static class ProfileProbe {}
}

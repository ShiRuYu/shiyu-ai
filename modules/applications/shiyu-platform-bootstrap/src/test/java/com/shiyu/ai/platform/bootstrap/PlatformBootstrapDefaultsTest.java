package com.shiyu.ai.platform.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

/**
 * 验证平台发行包提供可直接启动所需的默认配置。
 */
class PlatformBootstrapDefaultsTest {

    /**
     * 平台默认配置应启用事务、开发环境和默认 HTTP 端口。
     *
     * @throws IOException 读取默认配置失败时抛出。
     */
    @Test
    void defaultConfigurationProvidesDirectStartupValues() throws IOException {
        ConfigurableEnvironment environment = new StandardEnvironment();
        List<PropertySource<?>> sources =
                new YamlPropertySourceLoader()
                        .load("platform-defaults", new ClassPathResource("application.yml"));
        sources.forEach(source -> environment.getPropertySources().addLast(source));

        assertEquals("true", environment.getProperty("shiyu.tx.enabled"));
        assertEquals("9000", environment.getProperty("server.port"));
        assertEquals(
                "dev",
                environment.resolvePlaceholders(
                        environment.getRequiredProperty("spring.profiles.active")));
        assertEquals("h2", environment.getProperty("shiyu.infrastructure.database.provider"));
    }
}

package com.shiyu.ai.common.mybatis.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * 验证 SQL 代理配置引用的组件在 MyBatis 模块运行类路径中可加载。
 */
class SpyConfigurationTest {

    @Test
    void configuredP6SpyComponentsAreAvailable() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getResourceAsStream("/spy.properties")) {
            assertNotNull(stream, "spy.properties 必须随模块打包");
            properties.load(stream);
        }

        String[] classNames = {
            properties.getProperty("modulelist"),
            properties.getProperty("logMessageFormat"),
            properties.getProperty("appender")
        };
        Arrays.stream(classNames)
                .filter(value -> value != null && !value.isBlank())
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .forEach(name -> assertDoesNotThrow(() -> Class.forName(name), name));
    }
}

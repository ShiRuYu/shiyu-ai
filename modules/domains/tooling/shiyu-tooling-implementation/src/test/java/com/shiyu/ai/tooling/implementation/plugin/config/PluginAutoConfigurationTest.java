package com.shiyu.ai.tooling.implementation.plugin.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * 验证 插件 Auto Configuration 相关功能、边界条件、异常路径和协作行为。
 */
class PluginAutoConfigurationTest {
    @Test
    void exposesSandboxAndRegistryBeans() {
        PluginAutoConfiguration configuration = new PluginAutoConfiguration();
        assertNotNull(configuration.pluginSandbox());
        assertNotNull(configuration.pluginRegistry());
    }
}

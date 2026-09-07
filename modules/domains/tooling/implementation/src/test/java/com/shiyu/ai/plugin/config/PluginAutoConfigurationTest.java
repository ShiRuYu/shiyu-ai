package com.shiyu.ai.plugin.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PluginAutoConfigurationTest {
    @Test
    void exposesSandboxAndRegistryBeans() {
        PluginAutoConfiguration configuration = new PluginAutoConfiguration();
        assertNotNull(configuration.pluginSandbox());
        assertNotNull(configuration.pluginRegistry());
    }
}

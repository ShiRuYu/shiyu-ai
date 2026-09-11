package com.shiyu.ai.tooling.implementation.plugin.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PluginAutoConfigurationTest {
    @Test
    void exposesSandboxAndRegistryBeans() {
        PluginAutoConfiguration configuration = new PluginAutoConfiguration();
        assertNotNull(configuration.pluginSandbox());
        assertNotNull(configuration.pluginRegistry());
    }
}

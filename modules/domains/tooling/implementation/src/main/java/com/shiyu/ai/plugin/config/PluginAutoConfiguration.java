package com.shiyu.ai.plugin.config;
import com.shiyu.ai.plugin.sandbox.PluginSandbox;

import com.shiyu.ai.plugin.registry.PluginRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "shiyu.plugin.enabled", havingValue = "true", matchIfMissing = true)
public class PluginAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PluginSandbox pluginSandbox() {
        return new PluginSandbox();
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginRegistry pluginRegistry() {
        String pluginsDir = System.getProperty("shiyu.plugins.dir", "plugins");
        log.info("初始化插件系统, 目录: {}", pluginsDir);
        return new PluginRegistry(pluginsDir);
    }
}

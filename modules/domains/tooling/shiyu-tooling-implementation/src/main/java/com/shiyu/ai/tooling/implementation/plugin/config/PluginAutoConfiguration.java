package com.shiyu.ai.tooling.implementation.plugin.config;

import com.shiyu.ai.tooling.implementation.plugin.registry.PluginRegistry;
import com.shiyu.ai.tooling.implementation.plugin.sandbox.PluginSandbox;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 定义 插件 Auto 基础设施或应用能力的配置项及装配规则。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "shiyu.plugin.enabled", havingValue = "true", matchIfMissing = true)
public class PluginAutoConfiguration {

    /**
     * 执行 插件 Auto 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Auto 相关操作生成的结果数据。
     */
    @Bean
    @ConditionalOnMissingBean
    public PluginSandbox pluginSandbox() {
        return new PluginSandbox();
    }

    /**
     * 执行 插件 Auto 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 Auto 相关操作生成的结果数据。
     */
    @Bean
    @ConditionalOnMissingBean
    public PluginRegistry pluginRegistry() {
        String pluginsDir = System.getProperty("shiyu.plugins.dir", "plugins");
        log.info("初始化插件系统, 目录: {}", pluginsDir);
        return new PluginRegistry(pluginsDir);
    }
}

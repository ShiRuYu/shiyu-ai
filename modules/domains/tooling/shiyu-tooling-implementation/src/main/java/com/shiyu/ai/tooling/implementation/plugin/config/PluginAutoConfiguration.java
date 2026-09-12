package com.shiyu.ai.tooling.implementation.plugin.config;

import com.shiyu.ai.tooling.implementation.plugin.registry.PluginRegistry;
import com.shiyu.ai.tooling.implementation.plugin.sandbox.PluginSandbox;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code PluginAutoConfiguration} 提供工具模块的配置项，并集中声明其默认值和运行约束。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "shiyu.plugin.enabled", havingValue = "true", matchIfMissing = true)
public class PluginAutoConfiguration {

    /**
     * {@code pluginSandbox} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean
    public PluginSandbox pluginSandbox() {
        return new PluginSandbox();
    }

    /**
     * {@code pluginRegistry} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Bean
    @ConditionalOnMissingBean
    public PluginRegistry pluginRegistry() {
        String pluginsDir = System.getProperty("shiyu.plugins.dir", "plugins");
        log.info("初始化插件系统, 目录: {}", pluginsDir);
        return new PluginRegistry(pluginsDir);
    }
}

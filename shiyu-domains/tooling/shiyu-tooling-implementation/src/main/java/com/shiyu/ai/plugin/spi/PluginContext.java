package com.shiyu.ai.plugin.spi;

import java.util.Map;

/**
 * 插件运行上下文
 */
public class PluginContext {

    private final String pluginId;
    private final String pluginDir;
    private final Map<String, Object> config;

    public PluginContext(String pluginId, String pluginDir, Map<String, Object> config) {
        this.pluginId = pluginId;
        this.pluginDir = pluginDir;
        this.config = config;
    }

    public String getPluginId() { return pluginId; }
    public String getPluginDir() { return pluginDir; }
    public Map<String, Object> getConfig() { return config; }
}

package com.shiyu.ai.tooling.implementation.plugin.spi;

import java.util.Map;

/** 插件运行上下文 */
public class PluginContext {

    /**
     * pluginId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String pluginId;
    /**
     * pluginDir 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String pluginDir;
    /**
     * 配置，表示当前对象中的对应属性。
     */
    private final Map<String, Object> config;

    /**
     * {@code PluginContext} 创建并初始化当前类型实例。
     *
     * @param pluginId 参数值，用于执行当前操作。
     * @param pluginDir 参数值，用于执行当前操作。
     * @param config 参数值，用于执行当前操作。
     */
    public PluginContext(String pluginId, String pluginDir, Map<String, Object> config) {
        this.pluginId = pluginId;
        this.pluginDir = pluginDir;
        this.config = config;
    }

    /**
     * {@code getPluginId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * {@code getPluginDir} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getPluginDir() {
        return pluginDir;
    }

    /**
     * {@code getConfig} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> getConfig() {
        return config;
    }
}

package com.shiyu.ai.tooling.implementation.plugin.spi;

import java.util.Map;

/**
 * 实现 插件 相关的业务处理、协作逻辑或基础设施能力。
 */
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginId 用于定位plugin的标识。
     * @param pluginDir 用于完成本次业务处理的 pluginDir 参数。
     * @param config 用于完成本次业务处理的 config 参数。
     */
    public PluginContext(String pluginId, String pluginDir, Map<String, Object> config) {
        this.pluginId = pluginId;
        this.pluginDir = pluginDir;
        this.config = config;
    }

    /**
     * 查询 插件 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 相关操作生成的结果数据。
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * 查询 插件 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 相关操作生成的结果数据。
     */
    public String getPluginDir() {
        return pluginDir;
    }

    /**
     * 查询 插件 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 相关操作生成的结果数据。
     */
    public Map<String, Object> getConfig() {
        return config;
    }
}

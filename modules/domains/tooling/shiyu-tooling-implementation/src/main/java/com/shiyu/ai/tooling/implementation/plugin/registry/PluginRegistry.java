package com.shiyu.ai.tooling.implementation.plugin.registry;

import com.shiyu.ai.tooling.implementation.plugin.lifecycle.PluginLoader;
import com.shiyu.ai.tooling.implementation.plugin.lifecycle.PluginManager;
import com.shiyu.ai.tooling.implementation.plugin.spi.Plugin;
import com.shiyu.ai.tooling.implementation.plugin.spi.PluginDescriptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

/** 插件注册表（高层封装） 自动扫描插件目录、生命周期管理 */
@Slf4j
public class PluginRegistry {

    /**
     * pluginManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PluginManager pluginManager;
    /**
     * pluginLoader 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final PluginLoader pluginLoader;
    /**
     * pluginsDir 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String pluginsDir;
    /**
     * inProcessEnabled 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final boolean inProcessEnabled;

    /**
     * {@code PluginRegistry} 创建并初始化当前类型实例。
     *
     * @param pluginsDir 参数值，用于执行当前操作。
     */
    public PluginRegistry(String pluginsDir) {
        this(
                pluginsDir,
                Boolean.parseBoolean(System.getProperty("shiyu.plugins.in-process", "false")));
    }

    /**
     * {@code PluginRegistry} 创建并初始化当前类型实例。
     *
     * @param pluginsDir 参数值，用于执行当前操作。
     * @param inProcessEnabled 参数值，用于执行当前操作。
     */
    public PluginRegistry(String pluginsDir, boolean inProcessEnabled) {
        this.pluginsDir = pluginsDir;
        this.inProcessEnabled = inProcessEnabled;
        this.pluginManager = new PluginManager(pluginsDir);
        this.pluginLoader = new PluginLoader();
    }

    /**
     * {@code init} 执行当前类型定义的业务操作。
     */
    @PostConstruct
    public void init() {
        log.info(
                "插件注册表初始化, pluginDirectoryConfigured={}",
                pluginsDir != null && !pluginsDir.isBlank());
        scanAndLoadPlugins();
    }

    /** 扫描并加载插件目录 */
    public void scanAndLoadPlugins() {
        if (!inProcessEnabled) {
            log.info("插件目录扫描已跳过：生产模式只允许通过受控 Worker RPC 执行插件");
            return;
        }
        Path dir = Path.of(pluginsDir);
        if (!dir.toFile().exists()) {
            boolean created = dir.toFile().mkdirs();
            log.info("插件目录不存在，已创建: created={}", created);
            return;
        }
        pluginLoader.loadFromDirectory(dir, pluginManager);
        log.info("插件扫描完成，已发现 {} 个插件", pluginManager.listPlugins().size());

        for (PluginDescriptor desc : pluginManager.listPlugins()) {
            try {
                pluginManager.start(desc.getId());
            } catch (Exception e) {
                log.warn(
                        "插件自动启动失败: pluginIdLength={}, errorType={}, errorMessageLength={}",
                        valueLength(desc.getId()),
                        e.getClass().getSimpleName(),
                        valueLength(e.getMessage()));
            }
        }
    }

    /**
     * {@code shutdown} 执行当前类型定义的业务操作。
     */
    @PreDestroy
    public void shutdown() {
        if (!inProcessEnabled) return;
        log.info("停止所有插件...");
        for (PluginDescriptor desc : pluginManager.listPlugins()) {
            pluginManager.stop(desc.getId());
            pluginLoader.closeClassLoader(desc.getId());
        }
    }

    /**
     * {@code install} 执行当前类型定义的业务操作。
     *
     * @param descriptor 参数值，用于执行当前操作。
     * @param plugin 参数值，用于执行当前操作。
     */
    public void install(PluginDescriptor descriptor, Plugin plugin) {
        if (!inProcessEnabled)
            throw new SecurityException("in-process plugins are disabled; use a Worker RPC plugin");
        pluginManager.install(descriptor, plugin);
    }

    /**
     * {@code start} 执行当前类型定义的业务操作。
     *
     * @param pluginId 参数值，用于执行当前操作。
     */
    public void start(String pluginId) {
        requireInProcess();
        pluginManager.start(pluginId);
    }

    /**
     * {@code stop} 执行当前类型定义的业务操作。
     *
     * @param pluginId 参数值，用于执行当前操作。
     */
    public void stop(String pluginId) {
        requireInProcess();
        pluginManager.stop(pluginId);
    }

    /**
     * {@code uninstall} 执行当前类型定义的业务操作。
     *
     * @param pluginId 参数值，用于执行当前操作。
     */
    public void uninstall(String pluginId) {
        requireInProcess();
        pluginManager.uninstall(pluginId);
    }

    /**
     * {@code getDescriptor} 查询并返回当前操作所需的数据。
     *
     * @param pluginId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public PluginDescriptor getDescriptor(String pluginId) {
        return pluginManager.getDescriptor(pluginId);
    }

    /**
     * {@code getPlugin} 查询并返回当前操作所需的数据。
     *
     * @param pluginId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Plugin getPlugin(String pluginId) {
        requireInProcess();
        return pluginManager.getPlugin(pluginId);
    }

    /**
     * {@code listPlugins} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<PluginDescriptor> listPlugins() {
        return pluginManager.listPlugins();
    }

    /**
     * {@code getPluginManager} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    private void requireInProcess() {
        if (!inProcessEnabled)
            throw new SecurityException("in-process plugins are disabled; use a Worker RPC plugin");
    }

    private static int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}

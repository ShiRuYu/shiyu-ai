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

/**
 * 管理 插件 相关的运行时状态、注册信息或临时数据。
 */
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginsDir 用于完成本次业务处理的 pluginsDir 参数。
     */
    public PluginRegistry(String pluginsDir) {
        this(
                pluginsDir,
                Boolean.parseBoolean(System.getProperty("shiyu.plugins.in-process", "false")));
    }

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginsDir 用于完成本次业务处理的 pluginsDir 参数。
     * @param inProcessEnabled 用于完成本次业务处理的 inProcessEnabled 参数。
     */
    public PluginRegistry(String pluginsDir, boolean inProcessEnabled) {
        this.pluginsDir = pluginsDir;
        this.inProcessEnabled = inProcessEnabled;
        this.pluginManager = new PluginManager(pluginsDir);
        this.pluginLoader = new PluginLoader();
    }

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
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
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param descriptor 用于完成本次业务处理的 descriptor 参数。
     * @param plugin 用于完成本次业务处理的 plugin 参数。
     */
    public void install(PluginDescriptor descriptor, Plugin plugin) {
        if (!inProcessEnabled)
            throw new SecurityException("in-process plugins are disabled; use a Worker RPC plugin");
        pluginManager.install(descriptor, plugin);
    }

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginId 用于定位plugin的标识。
     */
    public void start(String pluginId) {
        requireInProcess();
        pluginManager.start(pluginId);
    }

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginId 用于定位plugin的标识。
     */
    public void stop(String pluginId) {
        requireInProcess();
        pluginManager.stop(pluginId);
    }

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginId 用于定位plugin的标识。
     */
    public void uninstall(String pluginId) {
        requireInProcess();
        pluginManager.uninstall(pluginId);
    }

    /**
     * 查询 插件 相关业务数据，并返回处理结果。
     *
     * @param pluginId 用于定位plugin的标识。
     * @return 返回 插件 相关操作生成的结果数据。
     */
    public PluginDescriptor getDescriptor(String pluginId) {
        return pluginManager.getDescriptor(pluginId);
    }

    /**
     * 查询 插件 相关业务数据，并返回处理结果。
     *
     * @param pluginId 用于定位plugin的标识。
     * @return 返回 插件 相关操作生成的结果数据。
     */
    public Plugin getPlugin(String pluginId) {
        requireInProcess();
        return pluginManager.getPlugin(pluginId);
    }

    /**
     * 查询 插件 相关业务数据，并返回处理结果。
     *
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<PluginDescriptor> listPlugins() {
        return pluginManager.listPlugins();
    }

    /**
     * 查询 插件 相关业务数据，并返回处理结果。
     *
     * @return 返回 插件 相关操作生成的结果数据。
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

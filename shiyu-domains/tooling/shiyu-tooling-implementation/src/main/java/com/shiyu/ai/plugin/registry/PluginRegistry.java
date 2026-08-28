package com.shiyu.ai.plugin.registry;

import com.shiyu.ai.plugin.lifecycle.PluginLoader;
import com.shiyu.ai.plugin.lifecycle.PluginManager;
import com.shiyu.ai.plugin.spi.Plugin;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

/**
 * 插件注册表（高层封装）
 * 自动扫描插件目录、生命周期管理
 */
@Slf4j
public class PluginRegistry {

    private final PluginManager pluginManager;
    private final PluginLoader pluginLoader;
    private final String pluginsDir;
    private final boolean inProcessEnabled;

    public PluginRegistry(String pluginsDir) {
        this(pluginsDir, Boolean.parseBoolean(System.getProperty("shiyu.plugins.in-process", "false")));
    }

    public PluginRegistry(String pluginsDir, boolean inProcessEnabled) {
        this.pluginsDir = pluginsDir;
        this.inProcessEnabled = inProcessEnabled;
        this.pluginManager = new PluginManager(pluginsDir);
        this.pluginLoader = new PluginLoader();
    }

    @PostConstruct
    public void init() {
        log.info("插件注册表初始化, 插件目录: {}", pluginsDir);
        scanAndLoadPlugins();
    }

    /**
     * 扫描并加载插件目录
     */
    public void scanAndLoadPlugins() {
        if (!inProcessEnabled) {
            log.info("插件目录扫描已跳过：生产模式只允许通过受控 Worker RPC 执行插件");
            return;
        }
        Path dir = Path.of(pluginsDir);
        if (!dir.toFile().exists()) {
            boolean created = dir.toFile().mkdirs();
            log.info("插件目录不存在，已创建: {} ({})", pluginsDir, created);
            return;
        }
        pluginLoader.loadFromDirectory(dir, pluginManager);
        log.info("插件扫描完成，已发现 {} 个插件", pluginManager.listPlugins().size());

        for (PluginDescriptor desc : pluginManager.listPlugins()) {
            try {
                pluginManager.start(desc.getId());
            } catch (Exception e) {
                log.warn("插件自动启动失败: {} ({})", desc.getId(), e.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        if (!inProcessEnabled) return;
        log.info("停止所有插件...");
        for (PluginDescriptor desc : pluginManager.listPlugins()) {
            pluginManager.stop(desc.getId());
            pluginLoader.closeClassLoader(desc.getId());
        }
    }

    public void install(PluginDescriptor descriptor, Plugin plugin) {
        if (!inProcessEnabled) throw new SecurityException("in-process plugins are disabled; use a Worker RPC plugin");
        pluginManager.install(descriptor, plugin);
    }

    public void start(String pluginId) { requireInProcess(); pluginManager.start(pluginId); }
    public void stop(String pluginId) { requireInProcess(); pluginManager.stop(pluginId); }
    public void uninstall(String pluginId) { requireInProcess(); pluginManager.uninstall(pluginId); }

    public PluginDescriptor getDescriptor(String pluginId) {
        return pluginManager.getDescriptor(pluginId);
    }

    public Plugin getPlugin(String pluginId) { requireInProcess(); return pluginManager.getPlugin(pluginId); }
    public List<PluginDescriptor> listPlugins() { return pluginManager.listPlugins(); }
    public PluginManager getPluginManager() { return pluginManager; }

    private void requireInProcess() {
        if (!inProcessEnabled) throw new SecurityException("in-process plugins are disabled; use a Worker RPC plugin");
    }
}

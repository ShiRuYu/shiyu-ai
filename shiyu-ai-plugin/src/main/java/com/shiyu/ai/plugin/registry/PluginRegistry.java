package com.shiyu.ai.plugin.registry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.shiyu.ai.plugin.lifecycle.PluginManager;
import com.shiyu.ai.plugin.spi.Plugin;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import com.shiyu.ai.plugin.lifecycle.PluginLoader;
import com.shiyu.ai.plugin.lifecycle.PluginManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件注册表（高层封装）
 * 自动扫描插件目录、生命周期管理
 */
@Slf4j
public class PluginRegistry {

    private final PluginManager pluginManager;
    private final PluginLoader pluginLoader;
    private final String pluginsDir;

    public PluginRegistry(String pluginsDir) {
        this.pluginsDir = pluginsDir;
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
        Path dir = Path.of(pluginsDir);
        if (!dir.toFile().exists()) {
            boolean created = dir.toFile().mkdirs();
            log.info("插件目录不存在，已创建: {} ({})", pluginsDir, created);
            return;
        }
        pluginLoader.loadFromDirectory(dir, pluginManager);
        log.info("插件扫描完成，已发现 {} 个插件", pluginManager.listPlugins().size());

        // 自动启动所有插件
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
        log.info("停止所有插件...");
        for (PluginDescriptor desc : pluginManager.listPlugins()) {
            pluginManager.stop(desc.getId());
        }
    }

    // ======================== 委托方法 ========================

    public void install(PluginDescriptor descriptor, Plugin plugin) {
        pluginManager.install(descriptor, plugin);
    }

    public void start(String pluginId) { pluginManager.start(pluginId); }
    public void stop(String pluginId) { pluginManager.stop(pluginId); }
    public void uninstall(String pluginId) { pluginManager.uninstall(pluginId); }

    public PluginDescriptor getDescriptor(String pluginId) {
        return pluginManager.getDescriptor(pluginId);
    }

    public Plugin getPlugin(String pluginId) { return pluginManager.getPlugin(pluginId); }
    public List<PluginDescriptor> listPlugins() { return pluginManager.listPlugins(); }
    public PluginManager getPluginManager() { return pluginManager; }
}

package com.shiyu.ai.plugin.lifecycle;

import com.shiyu.ai.plugin.spi.Plugin;
import com.shiyu.ai.plugin.spi.PluginContext;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import com.shiyu.ai.plugin.spi.PluginDescriptor.PluginState;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件管理器
 * 管理插件的完整生命周期：安装 → 启动 → 停止 → 卸载
 */
@Slf4j
public class PluginManager {

    private final Map<String, PluginDescriptor> descriptors = new ConcurrentHashMap<>();
    private final Map<String, Plugin> instances = new ConcurrentHashMap<>();
    private final String pluginsDir;

    public PluginManager(String pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    /**
     * 安装插件
     */
    public void install(PluginDescriptor descriptor, Plugin plugin) {
        if (descriptors.containsKey(descriptor.getId())) {
            throw new IllegalStateException("插件已存在: " + descriptor.getId());
        }
        descriptors.put(descriptor.getId(), descriptor);
        instances.put(descriptor.getId(), plugin);
        descriptor.setState(PluginState.INSTALLED);
        log.info("插件已安装: {} v{}", descriptor.getName(), descriptor.getVersion());
    }

    /**
     * 启动插件
     */
    public void start(String pluginId) {
        PluginDescriptor desc = descriptors.get(pluginId);
        Plugin plugin = instances.get(pluginId);
        if (desc == null || plugin == null) {
            throw new IllegalArgumentException("插件不存在: " + pluginId);
        }

        try {
            desc.setState(PluginState.STARTING);
            PluginContext context = new PluginContext(pluginId, pluginsDir + "/" + pluginId, desc.getConfig());
            plugin.init(context);
            plugin.start();
            desc.setState(PluginState.ACTIVE);
            log.info("插件已启动: {} v{}", desc.getName(), desc.getVersion());
        } catch (Exception e) {
            desc.setState(PluginState.FAILED);
            log.error("插件启动失败: {}", pluginId, e);
            throw new RuntimeException("插件启动失败: " + pluginId, e);
        }
    }

    /**
     * 停止插件
     */
    public void stop(String pluginId) {
        PluginDescriptor desc = descriptors.get(pluginId);
        Plugin plugin = instances.get(pluginId);
        if (desc == null || plugin == null) return;

        try {
            desc.setState(PluginState.STOPPING);
            plugin.stop();
            desc.setState(PluginState.STOPPED);
            log.info("插件已停止: {}", pluginId);
        } catch (Exception e) {
            log.error("插件停止失败: {}", pluginId, e);
        }
    }

    /**
     * 卸载插件
     */
    public void uninstall(String pluginId) {
        stop(pluginId);
        descriptors.remove(pluginId);
        instances.remove(pluginId);
        log.info("插件已卸载: {}", pluginId);
    }

    /**
     * 获取插件描述
     */
    public PluginDescriptor getDescriptor(String pluginId) {
        return descriptors.get(pluginId);
    }

    /**
     * 获取插件实例
     */
    public Plugin getPlugin(String pluginId) {
        return instances.get(pluginId);
    }

    /**
     * 列出所有已安装插件
     */
    public List<PluginDescriptor> listPlugins() {
        return new ArrayList<>(descriptors.values());
    }

    /**
     * 列出指定状态的插件
     */
    public List<PluginDescriptor> listPluginsByState(PluginState state) {
        return descriptors.values().stream()
                .filter(d -> d.getState() == state)
                .toList();
    }
}

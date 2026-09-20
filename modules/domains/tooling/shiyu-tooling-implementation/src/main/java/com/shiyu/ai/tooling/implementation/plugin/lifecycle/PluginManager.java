package com.shiyu.ai.tooling.implementation.plugin.lifecycle;

import com.shiyu.ai.tooling.implementation.plugin.spi.Plugin;
import com.shiyu.ai.tooling.implementation.plugin.spi.PluginContext;
import com.shiyu.ai.tooling.implementation.plugin.spi.PluginDescriptor;
import com.shiyu.ai.tooling.implementation.plugin.spi.PluginDescriptor.PluginState;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 插件 相关的运行时状态、注册信息或临时数据。
 */
@Slf4j
public class PluginManager {

    private final Map<String, PluginDescriptor> descriptors = new ConcurrentHashMap<>();
    private final Map<String, Plugin> instances = new ConcurrentHashMap<>();
    /**
     * pluginsDir 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String pluginsDir;

    /**
     * 执行 插件 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param pluginsDir 用于完成本次业务处理的 pluginsDir 参数。
     */
    public PluginManager(String pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    /** 安装插件 */
    public void install(PluginDescriptor descriptor, Plugin plugin) {
        if (descriptors.containsKey(descriptor.getId())) {
            throw new IllegalStateException("插件已存在: " + descriptor.getId());
        }
        descriptors.put(descriptor.getId(), descriptor);
        instances.put(descriptor.getId(), plugin);
        descriptor.setState(PluginState.INSTALLED);
        log.info("插件已安装: {} v{}", descriptor.getName(), descriptor.getVersion());
    }

    /** 启动插件 */
    public void start(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException("插件 ID 不能为空");
        }
        PluginDescriptor desc = descriptors.get(pluginId);
        Plugin plugin = instances.get(pluginId);
        if (desc == null || plugin == null) {
            throw new IllegalArgumentException("插件不存在: " + pluginId);
        }

        try {
            desc.setState(PluginState.STARTING);
            PluginContext context =
                    new PluginContext(pluginId, pluginsDir + "/" + pluginId, desc.getConfig());
            plugin.init(context);
            plugin.start();
            desc.setState(PluginState.ACTIVE);
            log.info("插件已启动: {} v{}", desc.getName(), desc.getVersion());
        } catch (Exception e) {
            desc.setState(PluginState.FAILED);
            log.error(
                    "插件启动失败: pluginIdLength={}, errorType={}, errorMessageLength={}",
                    valueLength(pluginId),
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
            throw new RuntimeException("插件启动失败: " + pluginId, e);
        }
    }

    /** 停止插件 */
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
            log.error(
                    "插件停止失败: pluginIdLength={}, errorType={}, errorMessageLength={}",
                    valueLength(pluginId),
                    e.getClass().getSimpleName(),
                    valueLength(e.getMessage()));
        }
    }

    /** 卸载插件 */
    public void uninstall(String pluginId) {
        stop(pluginId);
        descriptors.remove(pluginId);
        instances.remove(pluginId);
        log.info("插件已卸载: {}", pluginId);
    }

    /** 获取插件描述 */
    public PluginDescriptor getDescriptor(String pluginId) {
        return descriptors.get(pluginId);
    }

    /** 获取插件实例 */
    public Plugin getPlugin(String pluginId) {
        return instances.get(pluginId);
    }

    /** 列出所有已安装插件 */
    public List<PluginDescriptor> listPlugins() {
        return new ArrayList<>(descriptors.values());
    }

    /** 列出指定状态的插件 */
    public List<PluginDescriptor> listPluginsByState(PluginState state) {
        return descriptors.values().stream().filter(d -> d.getState() == state).toList();
    }

    private int valueLength(String value) {
        return value == null ? 0 : value.length();
    }
}

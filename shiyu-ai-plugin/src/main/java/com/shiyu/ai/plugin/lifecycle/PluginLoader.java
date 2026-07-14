package com.shiyu.ai.plugin.lifecycle;

import com.shiyu.ai.plugin.spi.Plugin;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import com.shiyu.ai.plugin.spi.PluginDescriptor.PluginState;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件加载器
 * 从 JAR 文件热加载插件，管理 ClassLoader 生命周期
 */
@Slf4j
public class PluginLoader {

    /** 插件 ID → ClassLoader 映射，用于后续关闭 */
    private final Map<String, URLClassLoader> classLoaders = new ConcurrentHashMap<>();

    /**
     * 从 JAR 文件加载插件
     * @param jarPath JAR 文件路径
     * @param descriptor 插件描述符
     * @return 插件实例
     */
    public Plugin loadFromJar(Path jarPath, PluginDescriptor descriptor) {
        URLClassLoader classLoader = null;
        try {
            File jarFile = jarPath.toFile();
            if (!jarFile.exists()) {
                throw new IllegalArgumentException("JAR 文件不存在: " + jarPath);
            }

            classLoader = new URLClassLoader(
                    new URL[]{jarFile.toURI().toURL()},
                    getClass().getClassLoader()
            );

            // 通过 ServiceLoader 加载 Plugin 实现
            ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
            for (Plugin plugin : serviceLoader) {
                if (plugin.getId().equals(descriptor.getId())) {
                    descriptor.setState(PluginState.RESOLVED);
                    classLoaders.put(descriptor.getId(), classLoader);
                    log.info("插件已从 JAR 加载: {} v{}", plugin.getName(), plugin.getVersion());
                    return plugin;
                }
            }

            // 如果 ServiceLoader 没找到，尝试反射加载 entryClass
            if (descriptor.getEntryClass() != null) {
                Class<?> clazz = classLoader.loadClass(descriptor.getEntryClass());
                Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
                descriptor.setState(PluginState.RESOLVED);
                classLoaders.put(descriptor.getId(), classLoader);
                log.info("插件已通过反射加载: {} v{}", plugin.getName(), plugin.getVersion());
                return plugin;
            }

            throw new IllegalStateException("JAR 中未找到 Plugin 实现: " + jarPath);
        } catch (Exception e) {
            closeClassLoader(descriptor.getId());
            log.error("插件加载失败: {}", jarPath, e);
            descriptor.setState(PluginState.FAILED);
            throw new RuntimeException("插件加载失败: " + jarPath, e);
        }
    }

    /**
     * 从插件目录加载所有插件
     */
    public void loadFromDirectory(Path dir, PluginManager manager) {
        File[] jars = dir.toFile().listFiles((d, name) -> name.endsWith(".jar"));
        if (jars == null) return;

        for (File jar : jars) {
            try {
                PluginDescriptor descriptor = readDescriptorFromJar(jar.toPath());
                if (descriptor != null) {
                    Plugin plugin = loadFromJar(jar.toPath(), descriptor);
                    manager.install(descriptor, plugin);
                }
            } catch (Exception e) {
                log.warn("跳过加载插件 JAR: {} ({})", jar.getName(), e.getMessage());
            }
        }
    }

    /**
     * 关闭指定插件的 ClassLoader
     */
    public void closeClassLoader(String pluginId) {
        URLClassLoader cl = classLoaders.remove(pluginId);
        if (cl != null) {
            try {
                cl.close();
                log.debug("插件 ClassLoader 已关闭: {}", pluginId);
            } catch (Exception e) {
                log.warn("关闭插件 ClassLoader 失败: {}", pluginId, e);
            }
        }
    }

    /**
     * 关闭所有 ClassLoader
     */
    public void closeAll() {
        for (String pluginId : classLoaders.keySet()) {
            closeClassLoader(pluginId);
        }
    }

    private PluginDescriptor readDescriptorFromJar(Path jarPath) {
        String fileName = jarPath.getFileName().toString();
        String id = fileName.replace(".jar", "");
        return new PluginDescriptor(
                id, id, "1.0.0",
                "Auto-discovered plugin: " + id,
                "unknown", null, Map.of()
        );
    }
}

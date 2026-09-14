package com.shiyu.ai.tooling.implementation.plugin.worker;

import java.time.Duration;
import java.util.Set;

/**
 * {@code PluginWorkerSpec} 封装工具模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param executable executable 属性，表示该记录组件承载的数据。
 * @param allowedDirectories allowedDirectories 属性，表示该记录组件承载的数据。
 * @param allowedHosts allowedHosts 属性，表示该记录组件承载的数据。
 * @param environmentKeys environmentKeys 属性，表示该记录组件承载的数据。
 * @param timeout timeout 属性，表示该记录组件承载的数据。
 */
public record PluginWorkerSpec(
        String executable,
        Set<String> allowedDirectories,
        Set<String> allowedHosts,
        Set<String> environmentKeys,
        Duration timeout) {
    public PluginWorkerSpec {
        allowedDirectories = allowedDirectories == null ? Set.of() : Set.copyOf(allowedDirectories);
        allowedHosts = allowedHosts == null ? Set.of() : Set.copyOf(allowedHosts);
        environmentKeys = environmentKeys == null ? Set.of() : Set.copyOf(environmentKeys);
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    }
}

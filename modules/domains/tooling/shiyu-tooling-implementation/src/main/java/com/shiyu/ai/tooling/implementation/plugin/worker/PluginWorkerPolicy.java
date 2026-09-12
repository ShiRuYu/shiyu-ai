package com.shiyu.ai.tooling.implementation.plugin.worker;

import java.net.URI;
import java.nio.file.Path;

/**
 * 定义插件工作进程的资源、权限和隔离策略。
 */
public final class PluginWorkerPolicy {
    private PluginWorkerPolicy() {}

    /**
     * {@code validateExecutable} 校验当前操作的输入或状态是否满足约束。
     *
     * @param spec 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Path validateExecutable(PluginWorkerSpec spec) {
        if (spec.allowedDirectories().isEmpty())
            throw new SecurityException("worker allow-list is required");
        Path executable = Path.of(spec.executable()).toAbsolutePath().normalize();
        if (spec.allowedDirectories().stream()
                .map(path -> Path.of(path).toAbsolutePath().normalize())
                .noneMatch(executable::startsWith)) {
            throw new SecurityException("worker executable is outside the plugin allow-list");
        }
        return executable;
    }

    /**
     * {@code validateNetworkTarget} 校验当前操作的输入或状态是否满足约束。
     *
     * @param spec 参数值，用于执行当前操作。
     * @param target 参数值，用于执行当前操作。
     */
    public static void validateNetworkTarget(PluginWorkerSpec spec, URI target) {
        if (target == null
                || target.getHost() == null
                || spec.allowedHosts().stream().noneMatch(target.getHost()::equalsIgnoreCase)) {
            throw new SecurityException("worker network target is outside the allow-list");
        }
    }
}

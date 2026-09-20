package com.shiyu.ai.tooling.implementation.plugin.worker;

import java.net.URI;
import java.nio.file.Path;

/**
 * 校验或约束 插件 Worker 相关的请求、状态和访问规则。
 */
public final class PluginWorkerPolicy {
    private PluginWorkerPolicy() {}

    /**
     * 校验或判断 插件 Worker 相关业务数据，并返回处理结果。
     *
     * @param spec 用于完成本次业务处理的 spec 参数。
     * @return 返回 插件 Worker 相关操作生成的结果数据。
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
     * 校验或判断 插件 Worker 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param spec 用于完成本次业务处理的 spec 参数。
     * @param target 用于完成本次业务处理的 target 参数。
     */
    public static void validateNetworkTarget(PluginWorkerSpec spec, URI target) {
        if (target == null
                || target.getHost() == null
                || spec.allowedHosts().stream().noneMatch(target.getHost()::equalsIgnoreCase)) {
            throw new SecurityException("worker network target is outside the allow-list");
        }
    }
}

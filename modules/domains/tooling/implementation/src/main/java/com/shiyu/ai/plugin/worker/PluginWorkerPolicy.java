package com.shiyu.ai.plugin.worker;

import java.net.URI;
import java.nio.file.Path;

/** Fail-closed checks applied before a worker is launched or called. */
public final class PluginWorkerPolicy {
    private PluginWorkerPolicy() { }
    public static Path validateExecutable(PluginWorkerSpec spec) {
        if (spec.allowedDirectories().isEmpty()) throw new SecurityException("worker allow-list is required");
        Path executable = Path.of(spec.executable()).toAbsolutePath().normalize();
        if (spec.allowedDirectories().stream().map(path -> Path.of(path).toAbsolutePath().normalize()).noneMatch(executable::startsWith)) {
            throw new SecurityException("worker executable is outside the plugin allow-list");
        }
        return executable;
    }
    public static void validateNetworkTarget(PluginWorkerSpec spec, URI target) {
        if (target == null || target.getHost() == null || spec.allowedHosts().stream().noneMatch(target.getHost()::equalsIgnoreCase)) {
            throw new SecurityException("worker network target is outside the allow-list");
        }
    }
}

package com.shiyu.ai.plugin.worker;

import java.time.Duration;
import java.util.Set;

public record PluginWorkerSpec(String executable, Set<String> allowedDirectories,
                               Set<String> allowedHosts, Set<String> environmentKeys,
                               Duration timeout) {
    public PluginWorkerSpec {
        allowedDirectories = allowedDirectories == null ? Set.of() : Set.copyOf(allowedDirectories);
        allowedHosts = allowedHosts == null ? Set.of() : Set.copyOf(allowedHosts);
        environmentKeys = environmentKeys == null ? Set.of() : Set.copyOf(environmentKeys);
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    }
}

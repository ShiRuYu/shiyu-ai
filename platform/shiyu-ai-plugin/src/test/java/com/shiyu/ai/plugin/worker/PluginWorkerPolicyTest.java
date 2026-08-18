package com.shiyu.ai.plugin.worker;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginWorkerPolicyTest {
    @Test
    void executableMustStayInsideExplicitAllowList() {
        PluginWorkerSpec spec = new PluginWorkerSpec("C:/plugins/worker.exe", Set.of("C:/trusted"),
                Set.of(), Set.of(), Duration.ofSeconds(1));
        assertThrows(SecurityException.class, () -> PluginWorkerPolicy.validateExecutable(spec));
    }

    @Test
    void networkTargetMustMatchManifestHost() {
        PluginWorkerSpec spec = new PluginWorkerSpec("C:/trusted/worker.exe", Set.of("C:/trusted"),
                Set.of("api.example.com"), Set.of(), Duration.ofSeconds(1));
        assertThrows(SecurityException.class, () -> PluginWorkerPolicy.validateNetworkTarget(spec, URI.create("https://evil.example/")));
    }
}

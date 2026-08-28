package com.shiyu.ai.plugin.worker;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void acceptsAllowListedExecutableAndNetworkTarget() {
        PluginWorkerSpec spec = new PluginWorkerSpec("C:/trusted/worker.exe", Set.of("C:/trusted"),
                Set.of("api.example.com"), Set.of("PATH"), Duration.ofSeconds(1));
        assertDoesNotThrow(() -> PluginWorkerPolicy.validateExecutable(spec));
        assertDoesNotThrow(() -> PluginWorkerPolicy.validateNetworkTarget(spec, URI.create("https://API.EXAMPLE.COM/v1")));
        assertThrows(SecurityException.class, () -> PluginWorkerPolicy.validateNetworkTarget(spec, null));
        assertThrows(SecurityException.class, () -> PluginWorkerPolicy.validateExecutable(new PluginWorkerSpec("C:/trusted/worker.exe", Set.of(), Set.of(), Set.of(), null)));
    }

    @Test
    void workerSpecAppliesFailClosedDefaults() {
        PluginWorkerSpec spec = new PluginWorkerSpec(null, null, null, null, null);
        assertTrue(spec.allowedDirectories().isEmpty());
        assertTrue(spec.allowedHosts().isEmpty());
        assertTrue(spec.environmentKeys().isEmpty());
        assertEquals(Duration.ofSeconds(30), spec.timeout());
    }
}

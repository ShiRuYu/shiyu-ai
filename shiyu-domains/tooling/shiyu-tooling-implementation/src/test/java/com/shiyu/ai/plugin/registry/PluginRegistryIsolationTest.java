package com.shiyu.ai.plugin.registry;

import com.shiyu.ai.plugin.spi.Plugin;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginRegistryIsolationTest {
    @Test
    void productionRegistryRejectsInProcessInstall() throws Exception {
        Path directory = Files.createTempDirectory("shiyu-plugin");
        PluginRegistry registry = new PluginRegistry(directory.toString(), false);
        PluginDescriptor descriptor = new PluginDescriptor("demo", "Demo", "1.0.0", "", "", "", java.util.Map.of());
        assertThrows(SecurityException.class, () -> registry.install(descriptor, new Plugin() {
            public String getId() { return "demo"; }
            public String getName() { return "Demo"; }
            public String getVersion() { return "1.0.0"; }
        }));
    }
}

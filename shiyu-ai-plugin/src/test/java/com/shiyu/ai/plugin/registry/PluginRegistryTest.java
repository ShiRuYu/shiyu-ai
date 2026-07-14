package com.shiyu.ai.plugin.registry;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PluginRegistry 单元测试
 */
@Tag("dev")
class PluginRegistryTest {

    @TempDir
    Path tempDir;

    @Test
    void testInitCreatesPluginDir() {
        String dir = tempDir.resolve("plugins").toString();
        assertFalse(Path.of(dir).toFile().exists());

        PluginRegistry registry = new PluginRegistry(dir);
        // @PostConstruct init() is only called by Spring,
        // so call scanAndLoadPlugins manually to trigger directory creation
        registry.scanAndLoadPlugins();
        assertTrue(Path.of(dir).toFile().exists());
    }

    @Test
    void testInitWithExistingDir() {
        assertDoesNotThrow(() -> {
            PluginRegistry registry = new PluginRegistry(tempDir.toString());
        });
    }

    @Test
    void testListEmptyRegistry() {
        PluginRegistry registry = new PluginRegistry(tempDir.toString());
        assertTrue(registry.listPlugins().isEmpty());
    }

    @Test
    void testGetDescriptorOfNonexistent() {
        PluginRegistry registry = new PluginRegistry(tempDir.toString());
        assertNull(registry.getDescriptor("nonexistent"));
    }

    @Test
    void testGetPluginOfNonexistent() {
        PluginRegistry registry = new PluginRegistry(tempDir.toString());
        assertNull(registry.getPlugin("nonexistent"));
    }

    @Test
    void testStartNonexistentThrows() {
        PluginRegistry registry = new PluginRegistry(tempDir.toString());
        assertThrows(Exception.class, () -> registry.start("nonexistent"));
    }
}

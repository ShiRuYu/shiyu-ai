package com.shiyu.ai.plugin.lifecycle;

import com.shiyu.ai.plugin.spi.Plugin;
import com.shiyu.ai.plugin.spi.PluginContext;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import com.shiyu.ai.plugin.spi.PluginDescriptor.PluginState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PluginManager 单元测试
 */
@Tag("dev")
class PluginManagerTest {

    private PluginManager manager;

    @BeforeEach
    void setUp() {
        manager = new PluginManager("/tmp/plugins-test");
    }

    @Test
    void testInstall() {
        PluginDescriptor desc = new PluginDescriptor("test-1", "Test Plugin", "1.0", "desc", "author", null, Map.of());
        Plugin plugin = new TestPlugin("test-1", "Test Plugin", "1.0");

        manager.install(desc, plugin);
        assertEquals(1, manager.listPlugins().size());
        assertEquals(PluginState.INSTALLED, manager.getDescriptor("test-1").getState());
    }

    @Test
    void testInstallDuplicateThrows() {
        PluginDescriptor desc = new PluginDescriptor("dup", "Dup", "1.0", "", "", null, Map.of());
        manager.install(desc, new TestPlugin("dup", "Dup", "1.0"));
        assertThrows(IllegalStateException.class, () ->
                manager.install(desc, new TestPlugin("dup", "Dup", "1.0")));
    }

    @Test
    void testStartPlugin() {
        TestPlugin plugin = new TestPlugin("start-test", "StartTest", "1.0");
        PluginDescriptor desc = new PluginDescriptor("start-test", "StartTest", "1.0", "", "", null, Map.of());
        manager.install(desc, plugin);

        manager.start("start-test");

        assertEquals(PluginState.ACTIVE, manager.getDescriptor("start-test").getState());
        assertTrue(plugin.isInitialized());
        assertTrue(plugin.isStarted());
    }

    @Test
    void testStopPlugin() {
        TestPlugin plugin = new TestPlugin("stop-test", "StopTest", "1.0");
        PluginDescriptor desc = new PluginDescriptor("stop-test", "StopTest", "1.0", "", "", null, Map.of());
        manager.install(desc, plugin);
        manager.start("stop-test");

        manager.stop("stop-test");

        assertEquals(PluginState.STOPPED, manager.getDescriptor("stop-test").getState());
        assertTrue(plugin.isStopped());
    }

    @Test
    void testUninstallStopsAndRemoves() {
        TestPlugin plugin = new TestPlugin("uninstall-test", "Uninstall", "1.0");
        PluginDescriptor desc = new PluginDescriptor("uninstall-test", "Uninstall", "1.0", "", "", null, Map.of());
        manager.install(desc, plugin);
        manager.start("uninstall-test");

        manager.uninstall("uninstall-test");

        assertNull(manager.getDescriptor("uninstall-test"));
        assertTrue(plugin.isStopped());
    }

    @Test
    void testListPluginsByState() {
        PluginDescriptor d1 = new PluginDescriptor("a", "A", "1.0", "", "", null, Map.of());
        PluginDescriptor d2 = new PluginDescriptor("b", "B", "1.0", "", "", null, Map.of());
        manager.install(d1, new TestPlugin("a", "A", "1.0"));
        manager.install(d2, new TestPlugin("b", "B", "1.0"));
        manager.start("a");

        List<PluginDescriptor> active = manager.listPluginsByState(PluginState.ACTIVE);
        List<PluginDescriptor> installed = manager.listPluginsByState(PluginState.INSTALLED);

        assertEquals(1, active.size());
        assertEquals("a", active.get(0).getId());
        assertEquals(1, installed.size());
        assertEquals("b", installed.get(0).getId());
    }

    @Test
    void testGetPlugin() {
        TestPlugin plugin = new TestPlugin("get-test", "Get", "1.0");
        manager.install(new PluginDescriptor("get-test", "Get", "1.0", "", "", null, Map.of()), plugin);
        assertNotNull(manager.getPlugin("get-test"));
        assertNull(manager.getPlugin("nonexistent"));
    }

    @Test
    void testStartNonexistentThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.start("nonexistent"));
    }

    /** 测试用 Plugin 实现 */
    static class TestPlugin implements Plugin {
        private final String id;
        private final String name;
        private final String version;
        private boolean initialized;
        private boolean started;
        private boolean stopped;
        private boolean destroyed;

        TestPlugin(String id, String name, String version) {
            this.id = id;
            this.name = name;
            this.version = version;
        }

        @Override public String getId() { return id; }
        @Override public String getName() { return name; }
        @Override public String getVersion() { return version; }
        @Override public void init(PluginContext context) { initialized = true; }
        @Override public void start() { started = true; }
        @Override public void stop() { stopped = true; }
        @Override public void destroy() { destroyed = true; }
        boolean isInitialized() { return initialized; }
        boolean isStarted() { return started; }
        boolean isStopped() { return stopped; }
    }
}

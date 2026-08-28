package com.shiyu.ai.plugin;

import com.shiyu.ai.plugin.lifecycle.PluginManager;
import com.shiyu.ai.plugin.registry.PluginRegistry;
import com.shiyu.ai.plugin.sandbox.PluginSandbox;
import com.shiyu.ai.plugin.spi.Plugin;
import com.shiyu.ai.plugin.spi.PluginDescriptor;
import com.shiyu.ai.plugin.worker.PluginWorkerLauncher;
import com.shiyu.ai.plugin.worker.PluginWorkerPolicy;
import com.shiyu.ai.plugin.worker.PluginWorkerRpcClient;
import com.shiyu.ai.plugin.worker.PluginWorkerSpec;
import com.shiyu.ai.tool.ToolService;
import com.shiyu.ai.tool.mcp.McpToolDescriptor;
import com.shiyu.ai.tool.mcp.McpToolRegistry;
import com.shiyu.ai.tooling.web.McpToolController;
import com.shiyu.ai.tooling.web.PluginController;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ToolingLifecycleAndWebTest {
    @Test
    void managesPluginLifecycleAndRecordsFailures() {
        PluginManager manager = new PluginManager("plugins");
        PluginDescriptor descriptor = descriptor("demo");
        RecordingPlugin plugin = new RecordingPlugin("demo");
        manager.install(descriptor, plugin);
        assertEquals(PluginDescriptor.PluginState.INSTALLED, descriptor.getState());
        assertThrows(IllegalStateException.class, () -> manager.install(descriptor("demo"), plugin));
        manager.start("demo");
        assertEquals(PluginDescriptor.PluginState.ACTIVE, descriptor.getState());
        assertTrue(plugin.started);
        assertEquals(1, manager.listPluginsByState(PluginDescriptor.PluginState.ACTIVE).size());
        manager.stop("demo");
        assertEquals(PluginDescriptor.PluginState.STOPPED, descriptor.getState());
        manager.stop("missing");
        assertNull(manager.getPlugin("missing"));
        assertThrows(IllegalArgumentException.class, () -> manager.start(null));
        assertTrue(manager.listPluginsByState(PluginDescriptor.PluginState.FAILED).isEmpty());
        manager.uninstall("demo");
        assertNull(manager.getDescriptor("demo"));

        PluginDescriptor brokenDescriptor = descriptor("broken");
        manager.install(brokenDescriptor, new RecordingPlugin("broken") {
            @Override public void start() { throw new IllegalStateException("boom"); }
        });
        assertThrows(RuntimeException.class, () -> manager.start("broken"));
        assertEquals(PluginDescriptor.PluginState.FAILED, brokenDescriptor.getState());
        assertThrows(IllegalArgumentException.class, () -> manager.start("missing"));

        PluginDescriptor stopBroken = descriptor("stop-broken");
        manager.install(stopBroken, new RecordingPlugin("stop-broken") {
            @Override public void stop() { throw new IllegalStateException("stop boom"); }
        });
        manager.stop("stop-broken");
        assertEquals(PluginDescriptor.PluginState.STOPPING, stopBroken.getState());

        PluginDescriptor metadata = descriptor("meta");
        assertEquals("meta", metadata.getId());
        metadata.setState(PluginDescriptor.PluginState.ACTIVE);
        assertEquals(PluginDescriptor.PluginState.ACTIVE, metadata.getState());
        com.shiyu.ai.plugin.spi.PluginContext context = new com.shiyu.ai.plugin.spi.PluginContext("meta", "plugins/meta", Map.of("x", 1));
        assertEquals("meta", context.getPluginId());
        assertEquals("plugins/meta", context.getPluginDir());
        assertEquals(1, context.getConfig().get("x"));
        Plugin defaults = new Plugin() {
            @Override public String getId() { return "default"; }
            @Override public String getName() { return "default"; }
            @Override public String getVersion() { return "1"; }
        };
        defaults.init(context); defaults.start(); defaults.stop(); defaults.destroy();
        assertEquals("not_implemented", ((Map<?, ?>) defaults.execute("noop", Map.of())).get("status"));
    }

    @Test
    void registryFailsClosedUnlessInProcessModeIsExplicit() throws Exception {
        PluginRegistry isolated = new PluginRegistry(Files.createTempDirectory("shiyu-plugin").toString(), false);
        isolated.init();
        assertThrows(SecurityException.class, () -> isolated.start("demo"));
        assertThrows(SecurityException.class, () -> isolated.getPlugin("demo"));
        assertTrue(isolated.listPlugins().isEmpty());

        PluginRegistry enabled = new PluginRegistry(Files.createTempDirectory("shiyu-plugin").toString(), true);
        enabled.init();
        RecordingPlugin plugin = new RecordingPlugin("demo");
        enabled.install(descriptor("demo"), plugin);
        assertSame(plugin, enabled.getPlugin("demo"));
        enabled.start("demo");
        enabled.stop("demo");
        enabled.uninstall("demo");
        enabled.shutdown();
    }

    @Test
    void sandboxAllowsNormalWorkAndRejectsBlockedOrFailedCalls() {
        PluginSandbox sandbox = new PluginSandbox();
        assertEquals("ok", sandbox.executeInSandbox("demo", () -> "ok"));
        assertThrows(SecurityException.class, () -> sandbox.checkPermission("demo", "java.net.Socket"));
        assertDoesNotThrow(() -> sandbox.checkPermission("demo", "java.util.List"));
        assertThrows(RuntimeException.class, () -> sandbox.executeInSandbox("demo", () -> { throw new Exception("boom"); }));
        assertThrows(SecurityException.class, () -> sandbox.executeInSandbox("demo", () -> { throw new SecurityException("blocked"); }));
    }

    @Test
    void workerLaunchAndRpcAreFailClosed() throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java");
        PluginWorkerSpec spec = new PluginWorkerSpec(java.toString(),
                Set.of(java.getParent().toString()), Set.of("localhost"), Set.of(), Duration.ofSeconds(5));
        assertEquals(java.toAbsolutePath().normalize(), PluginWorkerPolicy.validateExecutable(spec));
        PluginWorkerPolicy.validateNetworkTarget(spec, URI.create("http://localhost/health"));
        assertThrows(SecurityException.class, () -> PluginWorkerPolicy.validateNetworkTarget(spec, URI.create("http://example.com")));
        assertThrows(SecurityException.class, () -> PluginWorkerPolicy.validateExecutable(new PluginWorkerSpec(java.toString(), Set.of(), Set.of(), Set.of(), Duration.ZERO)));

        Process version = PluginWorkerLauncher.launch(spec, List.of("-version"));
        assertEquals(0, PluginWorkerLauncher.await(version, spec));
        Process echo = new ProcessBuilder(java.toString(), "-cp", System.getProperty("java.class.path"), RpcEchoMain.class.getName()).start();
        try {
            assertEquals("pong", PluginWorkerRpcClient.call(echo, "ping", spec));
            assertThrows(SecurityException.class, () -> PluginWorkerRpcClient.call(echo, "bad\nrequest", spec));
        } finally {
            echo.destroyForcibly();
        }
    }

    @Test
    void loadsServiceProviderJarsAndMarksFailures() throws Exception {
        Path jar = Files.createTempFile("demo", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("META-INF/services/com.shiyu.ai.plugin.spi.Plugin"));
            output.write((JarPlugin.class.getName() + "\n").getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        com.shiyu.ai.plugin.lifecycle.PluginLoader loader = new com.shiyu.ai.plugin.lifecycle.PluginLoader();
        PluginDescriptor descriptor = descriptor("jar-demo");
        assertEquals("jar-demo", loader.loadFromJar(jar, descriptor).getId());
        assertEquals(PluginDescriptor.PluginState.RESOLVED, descriptor.getState());
        loader.closeClassLoader("jar-demo");
        loader.closeAll();

        PluginDescriptor missing = descriptor("missing");
        assertThrows(RuntimeException.class, () -> loader.loadFromJar(jar.resolveSibling("missing.jar"), missing));
        assertEquals(PluginDescriptor.PluginState.FAILED, missing.getState());

        Path directory = Files.createTempDirectory("plugin-dir");
        Files.copy(jar, directory.resolve("jar-demo.jar"));
        PluginManager manager = new PluginManager(directory.toString());
        loader.loadFromDirectory(directory, manager);
        assertEquals(1, manager.listPlugins().size());

        PluginDescriptor reflected = new PluginDescriptor("reflect-demo", "Reflect", "1", "", "test",
                JarPlugin.class.getName(), Map.of());
        assertEquals("jar-demo", loader.loadFromJar(jar, reflected).getId());
        Path emptyJar = Files.createTempFile("empty", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(emptyJar))) {
            output.flush();
        }
        assertThrows(RuntimeException.class, () -> loader.loadFromJar(emptyJar, descriptor("empty")));
    }

    @Test
    void mapsMcpQueriesAndPluginOperationsToResultResponses() {
        McpToolRegistry tools = new McpToolRegistry();
        McpToolDescriptor descriptor = new McpToolDescriptor("search", "Search", "mcp", Map.of(), List.of("web"), "network", false);
        tools.register(descriptor);
        ToolService toolService = mock(ToolService.class);
        when(toolService.execute(eq("search"), any())).thenReturn(new ToolService.ToolExecutionResult(true, Map.of("ok", true), null));
        McpToolController mcp = new McpToolController(tools, toolService);
        assertNotNull(mcp.listTools(null, null, null));
        assertNotNull(mcp.listTools("network", null, null));
        assertNotNull(mcp.listTools(null, "web", null));
        assertNotNull(mcp.listTools(null, null, "sea"));
        assertNotNull(mcp.getTool("search"));
        assertNotNull(mcp.getTool("missing"));
        assertNotNull(mcp.executeTool("search", Map.of()));
        assertNotNull(mcp.executeTool("missing", Map.of()));
        assertNotNull(mcp.getCategories());
        assertNotNull(mcp.getStats());
        when(toolService.execute(eq("search"), any())).thenReturn(new ToolService.ToolExecutionResult(false, null, "failed"));
        assertNotNull(mcp.executeTool("search", Map.of()));

        PluginRegistry registry = mock(PluginRegistry.class);
        PluginController plugins = new PluginController(registry, mock(com.shiyu.ai.plugin.market.PluginMarketService.class));
        when(registry.listPlugins()).thenReturn(List.of(descriptor("demo")));
        assertNotNull(plugins.listPlugins());
        assertNotNull(plugins.startPlugin("demo"));
        assertNotNull(plugins.stopPlugin("demo"));
        assertNotNull(plugins.uninstallPlugin("demo"));
        assertNotNull(plugins.rescan());
        doThrow(new IllegalStateException("boom")).when(registry).start("broken");
        assertNotNull(plugins.startPlugin("broken"));
    }

    private static PluginDescriptor descriptor(String id) {
        return new PluginDescriptor(id, id, "1.0.0", "", "test", null, Map.of());
    }

    private static class RecordingPlugin implements Plugin {
        private final String id;
        private boolean started;
        private RecordingPlugin(String id) { this.id = id; }
        @Override public String getId() { return id; }
        @Override public String getName() { return id; }
        @Override public String getVersion() { return "1.0.0"; }
        @Override public void start() { started = true; }
    }

    public static final class RpcEchoMain {
        public static void main(String[] args) throws Exception {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
            if (reader.readLine() != null) {
                System.out.println("pong");
                System.out.flush();
            }
        }
    }

    public static final class JarPlugin implements Plugin {
        public JarPlugin() { }
        @Override public String getId() { return "jar-demo"; }
        @Override public String getName() { return "Jar Demo"; }
        @Override public String getVersion() { return "1.0.0"; }
    }
}

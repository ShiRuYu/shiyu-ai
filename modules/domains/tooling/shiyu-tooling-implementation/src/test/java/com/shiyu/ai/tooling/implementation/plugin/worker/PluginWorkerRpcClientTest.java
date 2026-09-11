package com.shiyu.ai.tooling.implementation.plugin.worker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

class PluginWorkerRpcClientTest {
    @Test
    void exchangesNewlineDelimitedRequestAndRejectsInvalidPayloads() throws Exception {
        Path executable = javaExecutable();
        PluginWorkerSpec spec = spec(executable);
        Process worker = mock(Process.class);
        when(worker.isAlive()).thenReturn(true);
        when(worker.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(worker.getInputStream())
                .thenReturn(
                        new ByteArrayInputStream(
                                "response\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertEquals("response", PluginWorkerRpcClient.call(worker, "{\"ok\":true}", spec));
        assertThrows(SecurityException.class, () -> PluginWorkerRpcClient.call(worker, "", spec));
        assertThrows(
                SecurityException.class,
                () -> PluginWorkerRpcClient.call(worker, "bad\nrequest", spec));
    }

    @Test
    void rejectsDeadWorker() throws Exception {
        Path executable = javaExecutable();
        Process worker = new ProcessBuilder(executable.toString(), "-version").start();
        worker.waitFor();
        PluginWorkerSpec spec = spec(executable);
        assertThrows(
                java.io.IOException.class,
                () -> PluginWorkerRpcClient.call(worker, "request", spec));
    }

    @Test
    void timesOutAndDestroysWorkerWhenNoResponseArrives() throws Exception {
        Process worker = mock(Process.class);
        when(worker.isAlive()).thenReturn(true);
        when(worker.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(worker.getInputStream())
                .thenReturn(
                        new java.io.InputStream() {
                            @Override
                            public int read() {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ignored) {
                                }
                                return -1;
                            }
                        });
        PluginWorkerSpec spec =
                new PluginWorkerSpec("worker", Set.of(), Set.of(), Set.of(), Duration.ofMillis(1));
        assertThrows(
                java.util.concurrent.TimeoutException.class,
                () -> PluginWorkerRpcClient.call(worker, "request", spec));
        verify(worker).destroyForcibly();
    }

    private static Path javaExecutable() {
        String executableName =
                System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executableName);
    }

    private static PluginWorkerSpec spec(Path executable) {
        return new PluginWorkerSpec(
                executable.toString(),
                Set.of(executable.getParent().toString()),
                Set.of(),
                Set.of(),
                Duration.ofSeconds(1));
    }
}

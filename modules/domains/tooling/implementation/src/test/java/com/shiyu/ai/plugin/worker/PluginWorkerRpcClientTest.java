package com.shiyu.ai.plugin.worker;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class PluginWorkerRpcClientTest {
    @Test
    void exchangesNewlineDelimitedRequestAndRejectsInvalidPayloads() throws Exception {
        String executable = System.getenv().getOrDefault("ComSpec", "C:/Windows/System32/cmd.exe");
        PluginWorkerSpec spec = new PluginWorkerSpec(executable, Set.of(java.nio.file.Path.of(executable).getParent().toString()), Set.of(), Set.of(), Duration.ofSeconds(1));
        Process worker = mock(Process.class);
        when(worker.isAlive()).thenReturn(true);
        when(worker.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(worker.getInputStream()).thenReturn(new ByteArrayInputStream("response\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertEquals("response", PluginWorkerRpcClient.call(worker, "{\"ok\":true}", spec));
        assertThrows(SecurityException.class, () -> PluginWorkerRpcClient.call(worker, "", spec));
        assertThrows(SecurityException.class, () -> PluginWorkerRpcClient.call(worker, "bad\nrequest", spec));
    }

    @Test
    void rejectsDeadWorker() throws Exception {
        String executable = System.getenv().getOrDefault("ComSpec", "C:/Windows/System32/cmd.exe");
        Process worker = new ProcessBuilder(executable, "/c", "exit", "0").start();
        worker.waitFor();
        PluginWorkerSpec spec = new PluginWorkerSpec(executable, Set.of(java.nio.file.Path.of(executable).getParent().toString()), Set.of(), Set.of(), Duration.ofSeconds(1));
        assertThrows(java.io.IOException.class, () -> PluginWorkerRpcClient.call(worker, "request", spec));
    }

    @Test
    void timesOutAndDestroysWorkerWhenNoResponseArrives() throws Exception {
        Process worker = mock(Process.class);
        when(worker.isAlive()).thenReturn(true);
        when(worker.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(worker.getInputStream()).thenReturn(new java.io.InputStream() {
            @Override public int read() {
                try { Thread.sleep(500); } catch (InterruptedException ignored) { }
                return -1;
            }
        });
        PluginWorkerSpec spec = new PluginWorkerSpec("worker", Set.of(), Set.of(), Set.of(), Duration.ofMillis(1));
        assertThrows(java.util.concurrent.TimeoutException.class, () -> PluginWorkerRpcClient.call(worker, "request", spec));
        verify(worker).destroyForcibly();
    }
}

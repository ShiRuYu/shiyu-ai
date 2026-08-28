package com.shiyu.ai.plugin.worker;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class PluginWorkerLauncherTest {
    @Test
    void rejectsMissingExecutableAndUnsafeArguments() {
        PluginWorkerSpec missing = new PluginWorkerSpec(null, Set.of("C:/trusted"), Set.of(), Set.of(), Duration.ofSeconds(1));
        assertThrows(SecurityException.class, () -> PluginWorkerLauncher.launch(missing, List.of()));
        String executable = System.getenv().getOrDefault("ComSpec", "C:/Windows/System32/cmd.exe");
        PluginWorkerSpec spec = new PluginWorkerSpec(executable, Set.of(java.nio.file.Path.of(executable).getParent().toString()), Set.of(), Set.of(), Duration.ofSeconds(1));
        assertThrows(SecurityException.class, () -> PluginWorkerLauncher.launch(spec, List.of("..\\escape")));
        assertThrows(SecurityException.class, () -> PluginWorkerLauncher.launch(spec, java.util.Arrays.asList((String) null)));
    }

    @Test
    void awaitsSuccessfulWorkerAndTimesOutLongWorker() throws Exception {
        String executable = System.getenv().getOrDefault("ComSpec", "C:/Windows/System32/cmd.exe");
        Process success = new ProcessBuilder(executable, "/c", "exit", "0").start();
        PluginWorkerSpec spec = new PluginWorkerSpec(executable, Set.of(java.nio.file.Path.of(executable).getParent().toString()), Set.of(), Set.of(), Duration.ofSeconds(1));
        assertEquals(0, PluginWorkerLauncher.await(success, spec));

        Process slow = new ProcessBuilder(executable, "/c", "ping", "127.0.0.1", "-n", "5", ">", "nul").start();
        PluginWorkerSpec shortSpec = new PluginWorkerSpec(executable, Set.of(java.nio.file.Path.of(executable).getParent().toString()), Set.of(), Set.of(), Duration.ofMillis(1));
        assertThrows(TimeoutException.class, () -> PluginWorkerLauncher.await(slow, shortSpec));
    }
}

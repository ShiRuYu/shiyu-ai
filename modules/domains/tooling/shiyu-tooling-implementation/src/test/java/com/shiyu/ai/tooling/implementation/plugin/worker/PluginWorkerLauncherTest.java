package com.shiyu.ai.tooling.implementation.plugin.worker;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PluginWorkerLauncherTest {
    @Test
    void rejectsMissingExecutableAndUnsafeArguments() {
        PluginWorkerSpec missing = new PluginWorkerSpec(null, Set.of("C:/trusted"), Set.of(), Set.of(), Duration.ofSeconds(1));
        assertThrows(SecurityException.class, () -> PluginWorkerLauncher.launch(missing, List.of()));
        Path executable = javaExecutable();
        PluginWorkerSpec spec = spec(executable, Duration.ofSeconds(1));
        assertThrows(SecurityException.class, () -> PluginWorkerLauncher.launch(spec, List.of("..\\escape")));
        assertThrows(SecurityException.class, () -> PluginWorkerLauncher.launch(spec, java.util.Arrays.asList((String) null)));
    }

    @Test
    void awaitsSuccessfulWorkerAndTimesOutLongWorker() throws Exception {
        Path executable = javaExecutable();
        Process success = new ProcessBuilder(executable.toString(), "-version").start();
        PluginWorkerSpec spec = spec(executable, Duration.ofSeconds(1));
        assertEquals(0, PluginWorkerLauncher.await(success, spec));

        Process slow = new ProcessBuilder(executable.toString(), "-cp", System.getProperty("java.class.path"), Sleeper.class.getName()).start();
        PluginWorkerSpec shortSpec = spec(executable, Duration.ofMillis(1));
        assertThrows(TimeoutException.class, () -> PluginWorkerLauncher.await(slow, shortSpec));
    }

    private static Path javaExecutable() {
        String executableName = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        return Path.of(System.getProperty("java.home"), "bin", executableName);
    }

    private static PluginWorkerSpec spec(Path executable, Duration timeout) {
        return new PluginWorkerSpec(executable.toString(), Set.of(executable.getParent().toString()), Set.of(), Set.of(), timeout);
    }

    public static final class Sleeper {
        public static void main(String[] args) throws InterruptedException {
            Thread.sleep(Duration.ofSeconds(5).toMillis());
        }
    }
}

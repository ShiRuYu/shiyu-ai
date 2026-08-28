package com.shiyu.ai.plugin.worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Launches a plugin outside the application ClassLoader; callers communicate via controlled RPC. */
public final class PluginWorkerLauncher {
    private PluginWorkerLauncher() {}
    public static Process launch(PluginWorkerSpec spec, List<String> args) throws IOException {
        if (spec.executable() == null || spec.executable().isBlank()) throw new SecurityException("worker executable is required");
        Path executable = PluginWorkerPolicy.validateExecutable(spec);
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        if (args != null) for (String arg : args) {
            if (arg == null || arg.contains("..") || arg.contains("\u0000")) throw new SecurityException("unsafe worker argument");
            command.add(arg);
        }
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().keySet().removeIf(key -> !spec.environmentKeys().contains(key));
        return builder.start();
    }

    public static int await(Process process, PluginWorkerSpec spec) throws InterruptedException, java.util.concurrent.TimeoutException {
        if (!process.waitFor(Math.max(1, spec.timeout().toMillis()), TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new java.util.concurrent.TimeoutException("plugin worker timed out");
        }
        return process.exitValue();
    }
}

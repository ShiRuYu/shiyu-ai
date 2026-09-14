package com.shiyu.ai.tooling.implementation.plugin.worker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 按沙箱策略启动插件工作进程并管理其生命周期。
 */
public final class PluginWorkerLauncher {
    private PluginWorkerLauncher() {}

    /**
     * {@code launch} 执行当前类型定义的业务操作。
     *
     * @param spec 参数值，用于执行当前操作。
     * @param args 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static Process launch(PluginWorkerSpec spec, List<String> args) throws IOException {
        if (spec.executable() == null || spec.executable().isBlank())
            throw new SecurityException("worker executable is required");
        Path executable = PluginWorkerPolicy.validateExecutable(spec);
        List<String> command = new ArrayList<>();
        command.add(executable.toString());
        if (args != null)
            for (String arg : args) {
                if (arg == null || arg.contains("..") || arg.contains("\u0000"))
                    throw new SecurityException("unsafe worker argument");
                command.add(arg);
            }
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().keySet().removeIf(key -> !spec.environmentKeys().contains(key));
        return builder.start();
    }

    /**
     * {@code await} 执行当前类型定义的业务操作。
     *
     * @param process 参数值，用于执行当前操作。
     * @param spec 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static int await(Process process, PluginWorkerSpec spec)
            throws InterruptedException, java.util.concurrent.TimeoutException {
        if (!process.waitFor(Math.max(1, spec.timeout().toMillis()), TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            throw new java.util.concurrent.TimeoutException("plugin worker timed out");
        }
        return process.exitValue();
    }
}

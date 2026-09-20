package com.shiyu.ai.tooling.implementation.plugin.worker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 实现 插件 Worker Launcher 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class PluginWorkerLauncher {
    private PluginWorkerLauncher() {}

    /**
     * 执行 插件 Worker Launcher 相关业务数据，并返回处理结果。
     *
     * @param spec 用于完成本次业务处理的 spec 参数。
     * @param args 用于完成本次业务处理的 args 参数。
     * @return 返回 插件 Worker Launcher 相关操作生成的结果数据。
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
     * 执行 插件 Worker Launcher 相关业务数据，并返回处理结果。
     *
     * @param process 用于完成本次业务处理的 process 参数。
     * @param spec 用于完成本次业务处理的 spec 参数。
     * @return 返回 插件 Worker Launcher 相关操作生成的结果数据。
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

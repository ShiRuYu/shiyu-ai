package com.shiyu.ai.tooling.implementation.plugin.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * 通过受控 RPC 通道调用插件工作进程。
 */
public final class PluginWorkerRpcClient {
    private PluginWorkerRpcClient() {}

    /**
     * {@code call} 执行当前类型定义的业务操作。
     *
     * @param process 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     * @param spec 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String call(Process process, String request, PluginWorkerSpec spec)
            throws IOException, TimeoutException {
        if (process == null || !process.isAlive())
            throw new IOException("plugin worker is not running");
        if (request == null
                || request.isBlank()
                || request.indexOf('\n') >= 0
                || request.getBytes(StandardCharsets.UTF_8).length > 1_048_576) {
            throw new SecurityException("invalid worker RPC request");
        }
        try {
            BufferedWriter writer =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    process.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(request);
            writer.write("\n");
            writer.flush();
        } catch (IOException ex) {
            throw new IOException("worker RPC write failed", ex);
        }
        ExecutorService executor =
                Executors.newSingleThreadExecutor(
                        r -> {
                            Thread t = new Thread(r, "plugin-worker-rpc");
                            t.setDaemon(true);
                            return t;
                        });
        try {
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream(), StandardCharsets.UTF_8));
            Future<String> response = executor.submit(reader::readLine);
            String line =
                    response.get(Math.max(1, spec.timeout().toMillis()), TimeUnit.MILLISECONDS);
            if (line == null) throw new IOException("plugin worker closed RPC channel");
            return line;
        } catch (ExecutionException ex) {
            throw new IOException("worker RPC read failed", ex.getCause());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("worker RPC interrupted", ex);
        } catch (TimeoutException ex) {
            process.destroyForcibly();
            throw ex;
        } finally {
            executor.shutdownNow();
        }
    }
}

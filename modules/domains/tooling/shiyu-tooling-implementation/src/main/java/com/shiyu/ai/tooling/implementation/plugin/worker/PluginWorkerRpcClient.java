package com.shiyu.ai.tooling.implementation.plugin.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * 实现 插件 Worker Rpc 相关的业务处理、协作逻辑或基础设施能力。
 */
public final class PluginWorkerRpcClient {
    private PluginWorkerRpcClient() {}

    /**
     * 调用 插件 Worker Rpc 相关业务数据，并返回处理结果。
     *
     * @param process 用于完成本次业务处理的 process 参数。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @param spec 用于完成本次业务处理的 spec 参数。
     * @return 返回 插件 Worker Rpc 相关操作生成的结果数据。
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

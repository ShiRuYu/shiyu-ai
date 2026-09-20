package com.shiyu.ai.agent.implementation.timeout;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 实现 Timeout Policy Impl 相关的业务处理、协作逻辑或基础设施能力。
 */
@Slf4j
public class TimeoutPolicyImpl implements TimeoutPolicy {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    r -> {
                        Thread t = new Thread(r, "agent-timeout-");
                        t.setDaemon(true);
                        return t;
                    });

    /**
     * 调用 Timeout Policy Impl 相关业务数据，并返回处理结果。
     *
     * @param callable 用于完成本次业务处理的 callable 参数。
     * @param config 用于完成本次业务处理的 config 参数。
     * @return 返回 Timeout Policy Impl 相关操作生成的结果数据。
     */
    @Override
    public <T> T executeWithTimeout(Callable<T> callable, TimeoutConfig config) throws Exception {
        Future<T> future = null;
        try {
            future = scheduler.submit(callable);
            return future.get(config.getNodeTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("节点执行超时 ({}ms)", config.getNodeTimeoutMs());
            if (future != null) {
                future.cancel(true);
            }
            throw new TimeoutException("节点执行超时: " + config.getNodeTimeoutMs() + "ms");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new RuntimeException("节点执行异常", cause);
        }
    }

    /**
     * {@code close} 释放或移除当前操作涉及的资源。
     */
    @Override
    public void close() {
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Agent 超时调度线程池未能正常关闭");
            }
        } catch (InterruptedException exception) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

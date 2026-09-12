package com.shiyu.ai.agent.implementation.timeout;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/** 默认超时策略实现 */
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
     * {@code executeWithTimeout} 执行当前模块定义的业务流程。
     *
     * @param callable 参数值，用于执行当前操作。
     * @param config 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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

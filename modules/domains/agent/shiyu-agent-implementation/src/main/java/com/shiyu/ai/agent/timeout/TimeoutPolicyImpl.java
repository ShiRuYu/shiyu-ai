package com.shiyu.ai.agent.timeout;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 默认超时策略实现
 */
@Slf4j
public class TimeoutPolicyImpl implements TimeoutPolicy {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors(),
        r -> {
            Thread t = new Thread(r, "agent-timeout-");
            t.setDaemon(true);
            return t;
        }
    );

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
}

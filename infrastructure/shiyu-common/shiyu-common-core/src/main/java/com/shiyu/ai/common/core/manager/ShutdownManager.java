package com.shiyu.ai.common.core.manager;

import com.shiyu.ai.common.core.utils.Threads;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 确保应用退出时能关闭后台线程
 */
@Slf4j
@Component
public class ShutdownManager {

    private final List<ExecutorService> executorServices;

    public ShutdownManager(List<ExecutorService> executorServices) {
        this.executorServices = executorServices;
    }

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * 停止所有后台线程池
     */
    private void shutdownAsyncManager() {
        for (ExecutorService executor : executorServices) {
            try {
                log.info("====关闭后台任务线程池: {}====", executor);
                Threads.shutdownAndAwaitTermination(executor);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}

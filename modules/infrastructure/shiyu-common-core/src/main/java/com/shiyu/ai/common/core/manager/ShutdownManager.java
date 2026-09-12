package com.shiyu.ai.common.core.manager;

import com.shiyu.ai.common.core.utils.Threads;

import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

/** 确保应用退出时能关闭后台线程 */
@Slf4j
@Component
public class ShutdownManager {

    /**
     * executorServices 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<ExecutorService> executorServices;

    /**
     * {@code ShutdownManager} 创建并初始化当前类型实例。
     *
     * @param executorServices 参数值，用于执行当前操作。
     */
    public ShutdownManager(List<ExecutorService> executorServices) {
        this.executorServices = executorServices;
    }

    /**
     * {@code destroy} 执行当前类型定义的业务操作。
     */
    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /** 停止所有后台线程池 */
    private void shutdownAsyncManager() {
        for (ExecutorService executor : executorServices) {
            try {
                log.info("====关闭后台任务线程池: {}====", executor);
                Threads.shutdownAndAwaitTermination(executor);
            } catch (Exception e) {
                log.error(
                        "Shutdown hook failed: errorType={}, errorMessageLength={}",
                        e.getClass().getSimpleName(),
                        e.getMessage() == null ? 0 : e.getMessage().length());
            }
        }
    }
}

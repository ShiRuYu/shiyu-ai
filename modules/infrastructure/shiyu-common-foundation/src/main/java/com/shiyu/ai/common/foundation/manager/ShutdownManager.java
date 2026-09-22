package com.shiyu.ai.common.foundation.manager;

import com.shiyu.ai.common.foundation.utils.Threads;

import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 管理 Shutdown 相关的运行时状态、注册信息或临时数据。
 */
@Slf4j
@Component
public class ShutdownManager {

    /**
     * executorServices 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<ExecutorService> executorServices;

    /**
     * 执行 Shutdown 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param executorServices 用于完成本次业务处理的 executorServices 参数。
     */
    public ShutdownManager(List<ExecutorService> executorServices) {
        this.executorServices = executorServices;
    }

    /**
     * 执行 Shutdown 相关业务操作，并维护必要的状态和协作关系。
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

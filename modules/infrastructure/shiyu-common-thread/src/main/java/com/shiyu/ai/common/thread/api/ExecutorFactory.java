package com.shiyu.ai.common.thread.api;

import java.util.concurrent.ExecutorService;

/**
 * 创建或提供 Executor 相关的业务组件和运行时能力。
 */
public interface ExecutorFactory {

    /**
     * 创建指定类型的执行器
     *
     * @param poolType 线程池类型
     * @param name 线程池名称
     * @return 线程池执行器
     */
    ExecutorService createExecutor(PoolType poolType, String name);
}

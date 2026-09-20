package com.shiyu.ai.common.thread.api;

import java.util.concurrent.ExecutorService;

/**
 * 管理 Thread Pool 相关的运行时状态、注册信息或临时数据。
 */
public interface ThreadPoolManager {

    /**
     * 获取执行器。
     *
     * @param name 名称。
     *
     * @return 处理结果。
     */
    ExecutorService getExecutor(String name);

    /**
     * 获取指定类型的线程池执行器
     *
     * @param poolType 线程池类型
     * @return 线程池执行器
     */
    ExecutorService getExecutor(PoolType poolType);

    /**
     * 获取指定名称的线程池执行器
     *
     * @param poolType 线程池类型
     * @param name 线程池名称
     * @return 线程池执行器
     */
    ExecutorService getExecutor(PoolType poolType, String name);

    /**
     * 获取默认线程池执行器
     *
     * @return 默认线程池执行器
     */
    ExecutorService getDefaultExecutor();
}


package com.shiyu.ai.common.thread.api;

import java.util.concurrent.ExecutorService;

/**
 * 线程池管理器接口
 * 提供统一的线程池管理能力，支持获取不同类型的线程池执行器
 */
public interface ThreadPoolManager {

    /**
     * Get a named executor using the matching {@code shiyu.thread.pools} configuration.
     * Falls back to the default pool configuration when the name is not configured.
     *
     * @param name configured pool name
     * @return managed executor
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

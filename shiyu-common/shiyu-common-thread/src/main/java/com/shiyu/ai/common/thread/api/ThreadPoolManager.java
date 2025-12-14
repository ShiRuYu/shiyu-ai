
package com.shiyu.ai.common.thread.api;

import java.util.concurrent.Executor;

/**
 * 线程池管理器接口
 * 提供统一的线程池管理能力，支持获取不同类型的线程池执行器
 */
public interface ThreadPoolManager {

    /**
     * 获取指定类型的线程池执行器
     * 
     * @param poolType 线程池类型
     * @return 线程池执行器
     */
    Executor getExecutor(PoolType poolType);

    /**
     * 获取指定名称的线程池执行器
     * 
     * @param poolType 线程池类型
     * @param name 线程池名称
     * @return 线程池执行器
     */
    Executor getExecutor(PoolType poolType, String name);

    /**
     * 获取默认线程池执行器
     * 
     * @return 默认线程池执行器
     */
    Executor getDefaultExecutor();
}

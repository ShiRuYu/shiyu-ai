
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;

import java.util.concurrent.Executor;

/**
 * 执行器工厂接口
 * 定义创建不同类型线程池执行器的规范
 */
public interface ExecutorFactory {

    /**
     * 创建指定类型的执行器
     * 
     * @param poolType 线程池类型
     * @param name 线程池名称
     * @return 线程池执行器
     */
    Executor createExecutor(PoolType poolType, String name);
}

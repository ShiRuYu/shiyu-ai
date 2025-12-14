
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 虚拟线程池工厂
 * 创建基于Java 21+虚拟线程的线程池
 */
public class VirtualExecutorFactory implements ExecutorFactory {

    /**
     * 检查当前JVM是否支持虚拟线程
     * 
     * @return 是否支持虚拟线程
     */
    public static boolean isSupported() {
        try {
            Class<?> threadBuilderClass = Class.forName("java.lang.Thread$Builder");
            Method ofVirtualMethod = Thread.class.getMethod("ofVirtual");
            return ofVirtualMethod != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Executor createExecutor(PoolType poolType, String name) {
        if (!isSupported()) {
            throw new UnsupportedOperationException("当前JVM不支持虚拟线程，请使用Java 21或更高版本");
        }

        switch (poolType) {
            case VIRTUAL:
                return createVirtualExecutor(name);
            case DEFAULT:
                // 默认使用虚拟线程实现
                return createVirtualExecutor(name);
            case CPU_INTENSIVE:
                // CPU密集型任务仍然使用平台线程
                return new PlatformExecutorFactory().createExecutor(poolType, name);
            case IO_INTENSIVE:
                // IO密集型任务非常适合虚拟线程
                return createVirtualExecutor(name);
            case SCHEDULED:
                // 调度任务仍然使用平台线程
                return new PlatformExecutorFactory().createExecutor(poolType, name);
            case PRIORITY:
                // 优先级任务使用平台线程
                return new PlatformExecutorFactory().createExecutor(poolType, name);
            case CUSTOM:
            default:
                return createVirtualExecutor(name);
        }
    }

    /**
     * 创建虚拟线程执行器
     * 
     * @param name 线程池名称
     * @return 虚拟线程执行器
     */
    private ExecutorService createVirtualExecutor(String name) {
        // 使用官方推荐的API创建虚拟线程池
        ThreadFactory factory = Thread.ofVirtual()
                .name("shiyu-" + name + "-virtual-", 0)
                .factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }
}

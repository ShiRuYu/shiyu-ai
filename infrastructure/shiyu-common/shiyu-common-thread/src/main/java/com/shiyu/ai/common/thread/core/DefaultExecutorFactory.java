
package com.shiyu.ai.common.thread.core;

import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.config.ThreadingProperties;

import java.util.concurrent.ExecutorService;

/**
 * 默认执行器工厂
 * 根据环境自动选择使用平台线程或虚拟线程
 */
public class DefaultExecutorFactory implements ExecutorFactory {

    private final ExecutorFactory platformExecutorFactory;
    private final ExecutorFactory virtualExecutorFactory;

    public DefaultExecutorFactory() {
        this(null);
    }

    public DefaultExecutorFactory(ThreadingProperties properties) {
        this.platformExecutorFactory = new PlatformExecutorFactory(properties);
        this.virtualExecutorFactory = VirtualExecutorFactory.isSupported() 
            ? new VirtualExecutorFactory() 
            : null;
    }

    @Override
    public ExecutorService createExecutor(PoolType poolType, String name) {
        // 如果支持虚拟线程且请求的是虚拟线程池，则使用虚拟线程工厂
        if (virtualExecutorFactory != null && poolType == PoolType.VIRTUAL) {
            return virtualExecutorFactory.createExecutor(poolType, name);
        }

        // 其他情况使用平台线程工厂
        return platformExecutorFactory.createExecutor(poolType, name);
    }
}

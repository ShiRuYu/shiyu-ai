package com.shiyu.ai.common.thread.executor;

import com.shiyu.ai.common.thread.api.ExecutorFactory;
import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.config.ThreadingProperties;

import java.util.concurrent.ExecutorService;

/** 默认执行器工厂 根据环境自动选择使用平台线程或虚拟线程 */
public class DefaultExecutorFactory implements ExecutorFactory {

    /**
     * platformExecutorFactory 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ExecutorFactory platformExecutorFactory;
    /**
     * virtualExecutorFactory 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ExecutorFactory virtualExecutorFactory;

    /**
     * {@code DefaultExecutorFactory} 创建并初始化当前类型实例。
     */
    public DefaultExecutorFactory() {
        this(null);
    }

    /**
     * {@code DefaultExecutorFactory} 创建并初始化当前类型实例。
     *
     * @param properties 参数值，用于执行当前操作。
     */
    public DefaultExecutorFactory(ThreadingProperties properties) {
        this.platformExecutorFactory = new PlatformExecutorFactory(properties);
        this.virtualExecutorFactory =
                VirtualExecutorFactory.isSupported() ? new VirtualExecutorFactory() : null;
    }

    /**
     * {@code createExecutor} 写入或更新当前模块中的业务数据。
     *
     * @param poolType 参数值，用于执行当前操作。
     * @param name 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

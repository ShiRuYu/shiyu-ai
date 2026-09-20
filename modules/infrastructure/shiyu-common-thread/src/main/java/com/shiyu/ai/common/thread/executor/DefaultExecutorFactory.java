package com.shiyu.ai.common.thread.executor;

import com.shiyu.ai.common.thread.api.ExecutorFactory;
import com.shiyu.ai.common.thread.api.PoolType;
import com.shiyu.ai.common.thread.config.ThreadingProperties;

import java.util.concurrent.ExecutorService;

/**
 * 创建或提供 Default Executor 相关的业务组件和运行时能力。
 */
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
     * 执行 Default Executor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param properties 用于完成本次业务处理的 properties 参数。
     */
    public DefaultExecutorFactory(ThreadingProperties properties) {
        this.platformExecutorFactory = new PlatformExecutorFactory(properties);
        this.virtualExecutorFactory =
                VirtualExecutorFactory.isSupported() ? new VirtualExecutorFactory() : null;
    }

    /**
     * 创建或保存 Default Executor 相关业务数据，并返回处理结果。
     *
     * @param poolType 用于完成本次业务处理的 poolType 参数。
     * @param name 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Default Executor 相关操作生成的结果数据。
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

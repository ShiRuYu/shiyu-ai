package com.shiyu.ai.common.thread.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 线程池 配置属性 */
@Data
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {
    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private boolean enabled;
    /**
     * corePoolSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int corePoolSize;
    /**
     * maxPoolSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int maxPoolSize;
    /**
     * queueCapacity 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int queueCapacity = 1024;
    /**
     * keepAliveSeconds 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int keepAliveSeconds = 60;
    /**
     * scheduledCorePoolSize 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int scheduledCorePoolSize;
}

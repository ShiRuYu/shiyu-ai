package com.shiyu.ai.common.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池 配置属性
 */
@Data
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {
    private boolean enabled;
    private int corePoolSize;
    private int maxPoolSize;
    private int queueCapacity = 1024;
    private int keepAliveSeconds = 60;
    private int scheduledCorePoolSize;
}

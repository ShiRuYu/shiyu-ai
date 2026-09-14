package com.shiyu.ai.bootstrap.retention;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LogRetentionProperties 配置属性，集中管理应用领域相关运行参数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "shiyu.retention.logs")
public class LogRetentionProperties {

    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private boolean enabled = true;
    /**
     * maxAgeDays 属性，保存当前对象中的业务数据或协作依赖。
     */
    private int maxAgeDays = 30;
    /**
     * maxTotalBytes 属性，保存当前对象中的业务数据或协作依赖。
     */
    private long maxTotalBytes = 2L * 1024 * 1024 * 1024;
}

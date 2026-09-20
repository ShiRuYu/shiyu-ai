package com.shiyu.ai.bootstrap.retention;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 定义 Log Retention 基础设施或应用能力的配置项及装配规则。
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

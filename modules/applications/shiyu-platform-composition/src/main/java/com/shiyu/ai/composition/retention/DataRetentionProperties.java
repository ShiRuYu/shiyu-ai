package com.shiyu.ai.composition.retention;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DataRetentionProperties 配置属性，集中管理应用领域相关运行参数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "shiyu.retention.data")
public class DataRetentionProperties {

    /**
     * 启用开关，表示当前对象中的对应属性。
     */
    private boolean enabled = true;
    /**
     * 执行天数，表示当前对象中的对应属性。
     */
    private int executionDays = 30;
    /**
     * 审计天数，表示当前对象中的对应属性。
     */
    private int auditDays = 180;
    /**
     * 用量天数，表示当前对象中的对应属性。
     */
    private int usageDays = 365;
    /**
     * 任务天数，表示当前对象中的对应属性。
     */
    private int taskDays = 30;
}

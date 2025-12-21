
package com.shiyu.ai.common.thread.config;

import com.shiyu.ai.common.thread.api.PoolType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程模块配置属性
 * 用于配置线程池相关参数
 */
@Data
@ConfigurationProperties(prefix = "shiyu.thread")
public class ThreadingProperties {

    /**
     * 是否启用指标收集
     */
    private boolean metricsEnabled = true;

    /**
     * 是否启用OpenTelemetry集成
     */
    private boolean otelEnabled = false;

    /**
     * 默认线程池配置
     */
    private PoolProperties defaultPool = new PoolProperties();

    /**
     * 命名线程池配置
     */
    private Map<String, PoolProperties> pools = new HashMap<>();



    /**
     * 线程池配置属性
     */
    @Data
    public static class PoolProperties {

        /**
         * 线程池类型
         */
        private PoolType type = PoolType.DEFAULT;

        /**
         * 核心线程数
         */
        private int coreSize = -1;

        /**
         * 最大线程数
         */
        private int maxSize = -1;

        /**
         * 线程空闲时间(秒)
         */
        private int keepAliveTime = 60;

        /**
         * 队列容量
         */
        private int queueCapacity = 100;

        /**
         * 拒绝策略
         */
        private RejectionPolicy rejectionPolicy = RejectionPolicy.CALLER_RUNS;

        /**
         * 是否允许核心线程超时
         */
        private boolean allowCoreThreadTimeOut = false;

        /**
         * 线程名称前缀
         */
        private String threadNamePrefix;


    }

    /**
     * 拒绝策略枚举
     */
    public enum RejectionPolicy {
        /**
         * 由调用线程执行该任务
         */
        CALLER_RUNS,

        /**
         * 抛出RejectedExecutionException
         */
        ABORT,

        /**
         * 丢弃队列中最前面的任务，然后重新尝试执行任务
         */
        DISCARD_OLDEST,

        /**
         * 直接丢弃任务
         */
        DISCARD
    }
}

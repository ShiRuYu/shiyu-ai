package com.shiyu.ai.agent.implementation.retry;

/** 重试配置 */
public class RetryConfig {

    /**
     * maxRetries 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int maxRetries;
    /**
     * initialDelayMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long initialDelayMs;
    /**
     * backoffMultiplier 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final double backoffMultiplier;

    /**
     * {@code RetryConfig} 创建并初始化当前类型实例。
     *
     * @param maxRetries 参数值，用于执行当前操作。
     * @param initialDelayMs 参数值，用于执行当前操作。
     * @param backoffMultiplier 参数值，用于执行当前操作。
     */
    public RetryConfig(int maxRetries, long initialDelayMs, double backoffMultiplier) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
    }

    /**
     * {@code defaultConfig} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static RetryConfig defaultConfig() {
        return new RetryConfig(3, 1000, 2.0);
    }

    /**
     * {@code getMaxRetries} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * {@code getInitialDelayMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    /**
     * {@code getBackoffMultiplier} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }
}

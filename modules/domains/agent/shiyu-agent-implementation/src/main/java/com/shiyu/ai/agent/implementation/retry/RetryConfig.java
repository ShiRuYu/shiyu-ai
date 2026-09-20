package com.shiyu.ai.agent.implementation.retry;

/**
 * 定义 Retry 基础设施或应用能力的配置项及装配规则。
 */
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
     * 执行 Retry 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param maxRetries 用于完成本次业务处理的 maxRetries 参数。
     * @param initialDelayMs 用于完成本次业务处理的 initialDelayMs 参数。
     * @param backoffMultiplier 用于完成本次业务处理的 backoffMultiplier 参数。
     */
    public RetryConfig(int maxRetries, long initialDelayMs, double backoffMultiplier) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
    }

    /**
     * 执行 Retry 相关业务数据，并返回处理结果。
     *
     * @return 返回 Retry 相关操作生成的结果数据。
     */
    public static RetryConfig defaultConfig() {
        return new RetryConfig(3, 1000, 2.0);
    }

    /**
     * 查询 Retry 相关业务数据，并返回处理结果。
     *
     * @return 返回 Retry 相关操作生成的结果数据。
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * 查询 Retry 相关业务数据，并返回处理结果。
     *
     * @return 返回 Retry 相关操作生成的结果数据。
     */
    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    /**
     * 查询 Retry 相关业务数据，并返回处理结果。
     *
     * @return 返回 Retry 相关操作生成的结果数据。
     */
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }
}

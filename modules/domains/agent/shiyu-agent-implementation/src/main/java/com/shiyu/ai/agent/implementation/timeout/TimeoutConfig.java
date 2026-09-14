package com.shiyu.ai.agent.implementation.timeout;

/** 超时配置 */
public class TimeoutConfig {

    /**
     * globalTimeoutMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long globalTimeoutMs;
    /**
     * nodeTimeoutMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long nodeTimeoutMs;

    /**
     * {@code TimeoutConfig} 创建并初始化当前类型实例。
     *
     * @param globalTimeoutMs 参数值，用于执行当前操作。
     * @param nodeTimeoutMs 参数值，用于执行当前操作。
     */
    public TimeoutConfig(long globalTimeoutMs, long nodeTimeoutMs) {
        this.globalTimeoutMs = globalTimeoutMs;
        this.nodeTimeoutMs = nodeTimeoutMs;
    }

    /**
     * {@code defaultConfig} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static TimeoutConfig defaultConfig() {
        return new TimeoutConfig(300000, 60000); // 全局5分钟，节点1分钟
    }

    /**
     * {@code getGlobalTimeoutMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getGlobalTimeoutMs() {
        return globalTimeoutMs;
    }

    /**
     * {@code getNodeTimeoutMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getNodeTimeoutMs() {
        return nodeTimeoutMs;
    }
}

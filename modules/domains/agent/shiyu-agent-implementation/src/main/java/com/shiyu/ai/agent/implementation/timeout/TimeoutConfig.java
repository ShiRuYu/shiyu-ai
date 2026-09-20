package com.shiyu.ai.agent.implementation.timeout;

/**
 * 定义 Timeout 基础设施或应用能力的配置项及装配规则。
 */
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
     * 执行 Timeout 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param globalTimeoutMs 用于完成本次业务处理的 globalTimeoutMs 参数。
     * @param nodeTimeoutMs 用于完成本次业务处理的 nodeTimeoutMs 参数。
     */
    public TimeoutConfig(long globalTimeoutMs, long nodeTimeoutMs) {
        this.globalTimeoutMs = globalTimeoutMs;
        this.nodeTimeoutMs = nodeTimeoutMs;
    }

    /**
     * 执行 Timeout 相关业务数据，并返回处理结果。
     *
     * @return 返回 Timeout 相关操作生成的结果数据。
     */
    public static TimeoutConfig defaultConfig() {
        return new TimeoutConfig(300000, 60000); // 全局5分钟，节点1分钟
    }

    /**
     * 查询 Timeout 相关业务数据，并返回处理结果。
     *
     * @return 返回 Timeout 相关操作生成的结果数据。
     */
    public long getGlobalTimeoutMs() {
        return globalTimeoutMs;
    }

    /**
     * 查询 Timeout 相关业务数据，并返回处理结果。
     *
     * @return 返回 Timeout 相关操作生成的结果数据。
     */
    public long getNodeTimeoutMs() {
        return nodeTimeoutMs;
    }
}

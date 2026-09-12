package com.shiyu.ai.agent.contract.runtime;

/**
 * ContextPolicy 接口，定义智能体模块的能力边界。
 */
public interface ContextPolicy {
    /**
     * 判断read是否满足条件。
     *
     * @param item item 参数。
     * @param query query 参数。
     *
     * @return 判断结果。
     */
    boolean canRead(ContextItem item, ContextQuery query);
}

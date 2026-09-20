package com.shiyu.ai.agent.contract.runtime;

/**
 * 校验或约束 Context 相关的请求、状态和访问规则。
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

package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.*;

import org.springframework.stereotype.Component;

/**
 * {@code DefaultContextPolicy} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
public class DefaultContextPolicy implements ContextPolicy {
    /**
     * {@code canRead} 校验当前操作的输入或状态是否满足约束。
     *
     * @param item 参数值，用于执行当前操作。
     * @param query 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean canRead(ContextItem item, ContextQuery query) {
        return item != null
                && query != null
                && item.accessScope() != null
                && !item.accessScope().isBlank();
    }
}

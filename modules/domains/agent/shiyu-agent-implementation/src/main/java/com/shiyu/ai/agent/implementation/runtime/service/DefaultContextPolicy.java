package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.contract.runtime.*;

import org.springframework.stereotype.Component;

/**
 * 校验或约束 Default Context 相关的请求、状态和访问规则。
 */
@Component
public class DefaultContextPolicy implements ContextPolicy {
    /**
     * 校验或判断 Default Context 相关业务数据，并返回处理结果。
     *
     * @param item 用于完成本次业务处理的 item 参数。
     * @param query 用于筛选目标数据的查询条件。
     * @return 返回本次条件判断是否成立。
     */
    @Override
    public boolean canRead(ContextItem item, ContextQuery query) {
        return item != null
                && query != null
                && item.accessScope() != null
                && !item.accessScope().isBlank();
    }
}

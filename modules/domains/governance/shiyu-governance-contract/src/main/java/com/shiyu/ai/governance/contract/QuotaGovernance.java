package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * QuotaGovernance 接口，定义治理模块的能力边界。
 */
public interface QuotaGovernance {

    /**
     * 变更当前业务对象的处理状态。
     *
     * @param actor 当前操作主体上下文。
     * @param request 请求参数。
     *
     * @return 操作结果。
     */
    QuotaDecision reserve(ActorContext actor, QuotaRequest request);

    /**
     * 变更当前业务对象的处理状态。
     *
     * @param actor 当前操作主体上下文。
     * @param reservationId 方法参数。
     * @param usage 方法参数。
     */
    void settle(ActorContext actor, long reservationId, QuotaUsage usage);

    /**
     * 变更当前业务对象的处理状态。
     *
     * @param actor 当前操作主体上下文。
     * @param reservationId 方法参数。
     */
    void release(ActorContext actor, long reservationId);
}

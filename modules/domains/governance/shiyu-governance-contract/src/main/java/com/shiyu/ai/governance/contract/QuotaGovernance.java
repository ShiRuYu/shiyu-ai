package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 定义配额预留、续期、结算和释放的协作边界。
 */
public interface QuotaGovernance {

    /**
     * 为请求预留所需配额。
     *
     * @param actor 请求配额的主体及租户上下文
     * @param request 请求预留的配额
     * @return 是否允许请求及其配额预留标识
     */
    QuotaDecision reserve(ActorContext actor, QuotaRequest request);

    /**
     * 通过租户内稳定的业务引用预留配额；相同引用重试时，返回仍然有效的既有预留。
     *
     * @param actor 请求配额的主体及租户上下文
     * @param request 请求预留的配额
     * @param referenceKey 稳定业务引用，例如生成运行标识
     * @return 既有或新建预留对应的准入决策
     */
    default QuotaDecision reserve(ActorContext actor, QuotaRequest request, String referenceKey) {
        return reserve(actor, request);
    }

    /**
     * 为仍在执行的业务续期配额预留。
     *
     * @param actor 续期配额的主体及租户上下文
     * @param referenceKey 预留配额时使用的稳定业务引用
     * @return 是否成功续期有效预留
     */
    default boolean renew(ActorContext actor, String referenceKey) {
        return false;
    }

    /** 返回配置的配额预留续期间隔，单位为毫秒。 */
    default long reservationRenewalIntervalMillis() {
        return 60_000L;
    }

    /**
     * 按数字标识结算配额预留。
     *
     * @param actor 结算配额的主体及租户上下文
     * @param reservationId 配额预留标识
     * @param usage 本次需要计量的实际用量
     */
    void settle(ActorContext actor, long reservationId, QuotaUsage usage);

    /**
     * 按租户内稳定的业务引用结算配额预留。实现可接受预留过期后的迟到结算，
     * 确保已完成的业务仍可计量；重复结算不得重复累计用量。
     *
     * @param actor 结算配额的主体及租户上下文
     * @param referenceKey 预留配额时使用的稳定业务引用
     * @param usage 本次需要计量的实际用量
     */
    default void settle(ActorContext actor, String referenceKey, QuotaUsage usage) {
        throw new UnsupportedOperationException("Reference-based quota settlement is unsupported");
    }

    /**
     * 按数字标识释放配额预留。
     *
     * @param actor 释放配额的主体及租户上下文
     * @param reservationId 配额预留标识
     */
    void release(ActorContext actor, long reservationId);

    /**
     * 按租户内稳定的业务引用释放配额预留。
     *
     * @param actor 释放配额的主体及租户上下文
     * @param referenceKey 预留配额时使用的稳定业务引用
     */
    default void release(ActorContext actor, String referenceKey) {
        throw new UnsupportedOperationException("Reference-based quota release is unsupported");
    }
}

package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * 定义 Quota 治理 相关的协作契约和调用边界。
 */
public interface QuotaGovernance {

    /**
     * 执行 Quota 治理 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param request 封装本次操作所需业务字段的请求对象。
     * @return 返回 Quota 治理 相关操作生成的结果数据。
     */
    QuotaDecision reserve(ActorContext actor, QuotaRequest request);

    /**
     * 更新或设置 Quota 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param reservationId 用于定位reservation的标识。
     * @param usage 用于完成本次业务处理的 usage 参数。
     */
    void settle(ActorContext actor, long reservationId, QuotaUsage usage);

    /**
     * 执行 Quota 治理 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param reservationId 用于定位reservation的标识。
     */
    void release(ActorContext actor, long reservationId);
}

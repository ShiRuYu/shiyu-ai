package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

/**
 * 定义 用量 治理 相关的协作契约和调用边界。
 */
public interface UsageGovernance {

    /**
     * 追加用量governance。
     *
     * @param actor 调用方上下文。
     * @param usage usage 参数。
     *
     * @return 处理结果。
     */
    UsageRecordResult record(ActorContext actor, DomainEventEnvelope<UsageMeasurement> usage);
}

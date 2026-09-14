package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

/**
 * UsageGovernance 接口，定义治理模块的能力边界。
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

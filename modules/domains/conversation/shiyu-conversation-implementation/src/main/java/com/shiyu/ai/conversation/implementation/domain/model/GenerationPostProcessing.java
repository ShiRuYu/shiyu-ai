package com.shiyu.ai.conversation.implementation.domain.model;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.TenantId;

/** 生成进入终态时建立的可重试副作用任务，包含当前领取信息。 */
public record GenerationPostProcessing(
        GenerationRun run,
        TenantId tenantId,
        long ownerUserId,
        QuotaAction quotaAction,
        UsageAction usageAction,
        boolean quotaDone,
        boolean usageDone,
        int attempts,
        String claimToken) {
    /** 定义生成终态后需要执行的配额结算或释放操作。 */
    public enum QuotaAction {
        NONE,
        SETTLE,
        RELEASE
    }
    /** 定义生成终态后需要记录的完成或失败用量事件。 */

    public enum UsageAction {
        NONE,
        COMPLETED,
        FAILED
    }
    /** 标识生成后处理中的配额步骤或用量记录步骤。 */

    public enum Step {
        QUOTA,
        USAGE
    }
}

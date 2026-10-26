package com.shiyu.ai.composition.integration.governance;

import com.shiyu.ai.conversation.contract.api.GenerationAdmission;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.implementation.application.GenerationAdmissionException;
import com.shiyu.ai.governance.contract.QuotaDecision;
import com.shiyu.ai.governance.contract.QuotaGovernance;
import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.governance.contract.QuotaUsage;
import com.shiyu.ai.kernel.context.ActorContext;

import org.springframework.stereotype.Component;

/**
 * 校验并控制 Quota 生成 相关请求是否允许进入处理流程。
 */
@Component
public class QuotaGenerationAdmission implements GenerationAdmission {
    /**
     * 配额，表示当前对象中的对应属性。
     */
    private final QuotaGovernance quota;

    /**
     * 执行 Quota 生成 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param quota 用于完成本次业务处理的 quota 参数。
     */
    public QuotaGenerationAdmission(QuotaGovernance quota) {
        this.quota = quota;
    }

    /**
     * 执行 Quota 生成 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param run 用于完成本次业务处理的 run 参数。
     * @param estimatedPromptTokens 用于完成本次业务处理的 estimatedPromptTokens 参数。
     */
    @Override
    public void reserve(ActorContext actor, GenerationRun run, int estimatedPromptTokens) {
        QuotaDecision decision =
                quota.reserve(
                        actor,
                        new QuotaRequest(Math.max(0, estimatedPromptTokens), 0),
                        run.id());
        if (!decision.allowed()) throw new GenerationAdmissionException(decision.errorCode());
    }

    /**
     * 更新或设置 Quota 生成 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param run 用于完成本次业务处理的 run 参数。
     */
    @Override
    public boolean renew(ActorContext actor, GenerationRun run) {
        return quota.renew(actor, run.id());
    }

    @Override
    public long renewalIntervalMillis() {
        return quota.reservationRenewalIntervalMillis();
    }

    @Override
    public void settle(ActorContext actor, GenerationRun run) {
        quota.settle(
                actor,
                run.id(),
                new QuotaUsage(safe(run.promptTokens()), safe(run.completionTokens())));
    }

    /**
     * 执行 Quota 生成 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param run 用于完成本次业务处理的 run 参数。
     */
    @Override
    public void release(ActorContext actor, GenerationRun run) {
        quota.release(actor, run.id());
    }

    private static int safe(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }

}

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在会话生成开始前执行配额检查和用量预留。
 */
@Component
public class QuotaGenerationAdmission implements GenerationAdmission {
    /**
     * 配额，表示当前对象中的对应属性。
     */
    private final QuotaGovernance quota;
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();

    /**
     * {@code QuotaGenerationAdmission} 创建并初始化当前类型实例。
     *
     * @param quota 参数值，用于执行当前操作。
     */
    public QuotaGenerationAdmission(QuotaGovernance quota) {
        this.quota = quota;
    }

    /**
     * {@code reserve} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param run 参数值，用于执行当前操作。
     * @param estimatedPromptTokens 参数值，用于执行当前操作。
     */
    @Override
    public void reserve(ActorContext actor, GenerationRun run, int estimatedPromptTokens) {
        QuotaDecision decision =
                quota.reserve(actor, new QuotaRequest(Math.max(0, estimatedPromptTokens), 0));
        if (!decision.allowed()) throw new GenerationAdmissionException(decision.errorCode());
        reservations.put(
                run.id(),
                new Reservation(decision.reservationId(), Math.max(0, estimatedPromptTokens)));
    }

    /**
     * {@code settle} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param run 参数值，用于执行当前操作。
     */
    @Override
    public void settle(ActorContext actor, GenerationRun run) {
        Reservation reservation = reservations.remove(run.id());
        if (reservation != null)
            quota.settle(
                    actor,
                    reservation.id(),
                    new QuotaUsage(
                            safe(run.promptTokens()) > 0
                                    ? safe(run.promptTokens())
                                    : reservation.estimatedPromptTokens(),
                            safe(run.completionTokens())));
    }

    /**
     * {@code release} 释放或移除当前操作涉及的资源。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param run 参数值，用于执行当前操作。
     */
    @Override
    public void release(ActorContext actor, GenerationRun run) {
        Reservation reservation = reservations.remove(run.id());
        if (reservation != null) quota.release(actor, reservation.id());
    }

    private static int safe(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }

    /**
     * {@code Reservation} 封装治理模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param id 标识，表示该记录组件承载的数据。
     * @param estimatedPromptTokens estimatedPromptTokens 属性，表示该记录组件承载的数据。
     */
    private record Reservation(long id, int estimatedPromptTokens) {}
}

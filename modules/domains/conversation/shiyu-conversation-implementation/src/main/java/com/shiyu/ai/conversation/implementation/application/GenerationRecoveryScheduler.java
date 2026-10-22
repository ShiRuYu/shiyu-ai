package com.shiyu.ai.conversation.implementation.application;

import com.shiyu.ai.conversation.contract.api.GenerationAdmission;
import com.shiyu.ai.conversation.implementation.domain.model.RecoveredGeneration;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.UserId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 回收超时生成，取消本地 Provider 流并释放其配额预留。 */
@Component
public class GenerationRecoveryScheduler {
    private final GenerationRepository generations;
    private final GenerationAdmission admission;
    private final GenerationRunner runner;
    private final long recoveryTimeoutMs;

    public GenerationRecoveryScheduler(
            GenerationRepository generations,
            GenerationAdmission admission,
            GenerationRunner runner,
            @Value("${shiyu.conversation.recovery-timeout-ms:300000}") long recoveryTimeoutMs) {
        this.generations = generations;
        this.admission = admission;
        this.runner = runner;
        this.recoveryTimeoutMs = Math.max(1000L, recoveryTimeoutMs);
    }

    @Scheduled(fixedDelayString = "${shiyu.conversation.recovery-delay-ms:30000}")
    public void recoverStaleGenerations() {
        for (RecoveredGeneration recovered :
                generations.recoverStaleGenerations(recoveryTimeoutMs)) {
            runner.cancel(recovered.run().id());
            admission.release(
                    new ActorContext(
                            recovered.tenantId(), new UserId(recovered.ownerUserId()), false),
                    recovered.run());
        }
    }
}

package com.shiyu.ai.conversation.implementation.application;

import com.shiyu.ai.conversation.contract.api.GenerationAdmission;
import com.shiyu.ai.conversation.implementation.domain.model.RecoveredGeneration;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 回收超时生成，取消本地 Provider 流并调度终态后处理任务。 */
@Component
public class GenerationRecoveryScheduler {
    private final GenerationRepository generations;
    private final GenerationAdmission admission;
    private final GenerationRunner runner;
    private final GenerationPostProcessingService postProcessing;
    private final long recoveryTimeoutMs;

    public GenerationRecoveryScheduler(
            GenerationRepository generations,
            GenerationAdmission admission,
            GenerationRunner runner,
            @Value("${shiyu.conversation.recovery-timeout-ms:300000}") long recoveryTimeoutMs) {
        this(generations, admission, runner, null, recoveryTimeoutMs);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public GenerationRecoveryScheduler(
            GenerationRepository generations,
            GenerationAdmission admission,
            GenerationRunner runner,
            GenerationPostProcessingService postProcessing,
            @Value("${shiyu.conversation.recovery-timeout-ms:300000}") long recoveryTimeoutMs) {
        this.generations = generations;
        this.admission = admission;
        this.runner = runner;
        this.postProcessing = postProcessing;
        this.recoveryTimeoutMs = Math.max(1000L, recoveryTimeoutMs);
    }

    @Scheduled(fixedDelayString = "${shiyu.conversation.recovery-delay-ms:30000}")
    public void recoverStaleGenerations() {
        for (RecoveredGeneration recovered :
                generations.recoverStaleGenerations(recoveryTimeoutMs)) {
            runner.cancel(recovered.run().id());
        }
        if (postProcessing != null) postProcessing.processDue(100);
    }
}

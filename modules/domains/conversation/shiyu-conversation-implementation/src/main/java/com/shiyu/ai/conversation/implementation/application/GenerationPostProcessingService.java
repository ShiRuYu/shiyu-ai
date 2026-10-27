package com.shiyu.ai.conversation.implementation.application;

import com.shiyu.ai.conversation.contract.api.GenerationAdmission;
import com.shiyu.ai.conversation.contract.api.GenerationUsageSink;
import com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 重试在生成进入终态时记录的配额和用量副作用。 */
@org.springframework.stereotype.Component
public final class GenerationPostProcessingService {
    private static final Logger log = LoggerFactory.getLogger(GenerationPostProcessingService.class);
    private static final long MAX_RETRY_DELAY_MILLIS = 300_000L;

    private final ConversationRepository conversations;
    private final GenerationAdmission admission;
    private final GenerationUsageSink usageSink;

    public GenerationPostProcessingService(
            ConversationRepository conversations,
            GenerationAdmission admission,
            GenerationUsageSink usageSink) {
        this.conversations = conversations;
        this.admission = admission;
        this.usageSink = usageSink;
    }

    public void processGeneration(String generationId) {
        try {
            conversations.claimGenerationPostProcessing(generationId).ifPresent(this::processClaimed);
        } catch (RuntimeException failure) {
            log.warn("Unable to claim generation post-processing: generationId={}", generationId, failure);
        }
    }

    public void processDue(int limit) {
        int batchSize = Math.min(Math.max(limit, 1), 100);
        for (int index = 0; index < batchSize; index++) {
            final java.util.Optional<GenerationPostProcessing> claimed;
            try {
                claimed = conversations.claimGenerationPostProcessingNext();
            } catch (RuntimeException failure) {
                log.warn("Unable to claim due generation post-processing", failure);
                return;
            }
            if (claimed.isEmpty()) return;
            processClaimed(claimed.get());
        }
    }

    private void processClaimed(GenerationPostProcessing pending) {
        try {
            TenantScope.withTenant(
                    pending.tenantId(),
                    () -> {
                        processClaimedInTenantScope(pending);
                        return null;
                    });
        } catch (RuntimeException failure) {
            log.warn(
                    "Generation post-processing remains queued: generationId={}, claimToken={}",
                    pending.run().id(),
                    pending.claimToken(),
                    failure);
        }
    }

    private void processClaimedInTenantScope(GenerationPostProcessing pending) {
        try {
            ActorContext actor =
                    new ActorContext(pending.tenantId(), new UserId(pending.ownerUserId()), false);
            if (!pending.quotaDone()) {
                switch (pending.quotaAction()) {
                    case SETTLE -> admission.settle(actor, pending.run());
                    case RELEASE -> admission.release(actor, pending.run());
                    case NONE -> { }
                }
                if (conversations.completeGenerationPostProcessing(
                                pending.run().id(),
                                GenerationPostProcessing.Step.QUOTA,
                                pending.claimToken())
                        != 1) {
                    return;
                }
            }
            if (!pending.usageDone()) {
                switch (pending.usageAction()) {
                    case COMPLETED ->
                            usageSink.completed(
                                    pending.run(),
                                    pending.tenantId(),
                                    new UserId(pending.ownerUserId()));
                    case FAILED -> usageSink.failed(pending.run());
                    case NONE -> { }
                }
                if (conversations.completeGenerationPostProcessing(
                                pending.run().id(),
                                GenerationPostProcessing.Step.USAGE,
                                pending.claimToken())
                        != 1) {
                    return;
                }
            }
        } catch (RuntimeException failure) {
            long retryDelay = retryDelay(pending.attempts());
            try {
                conversations.releaseGenerationPostProcessing(
                        pending.run().id(), pending.claimToken(), retryDelay);
            } catch (RuntimeException releaseFailure) {
                failure.addSuppressed(releaseFailure);
            }
            log.warn(
                    "Generation post-processing will retry: generationId={}, attempts={}, delayMs={}",
                    pending.run().id(),
                    pending.attempts() + 1,
                    retryDelay,
                    failure);
        }
    }

    private static long retryDelay(int attempts) {
        int exponent = Math.min(Math.max(attempts, 0), 8);
        return Math.min(MAX_RETRY_DELAY_MILLIS, 1_000L << exponent);
    }
}

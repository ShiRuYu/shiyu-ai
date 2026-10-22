package com.shiyu.ai.conversation.implementation.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.conversation.contract.api.GenerationAdmission;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.contract.model.GenerationStatus;
import com.shiyu.ai.conversation.implementation.domain.model.RecoveredGeneration;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class GenerationRecoverySchedulerTest {
    @Test
    void releasesAdmissionForEachSuccessfullyRecoveredGeneration() {
        GenerationRepository generations = mock(GenerationRepository.class);
        GenerationAdmission admission = mock(GenerationAdmission.class);
        GenerationRunner runner = mock(GenerationRunner.class);
        GenerationRun failed =
                new GenerationRun(
                        "g1",
                        "c1",
                        "m1",
                        null,
                        "speaker",
                        "OPENAI",
                        "gpt",
                        GenerationStatus.FAILED,
                        10,
                        0,
                        1000,
                        "SERVICE_RESTART",
                        2,
                        false,
                        3,
                        Instant.now().minusSeconds(10),
                        Instant.now(),
                        null);
        when(generations.recoverStaleGenerations(5000))
                .thenReturn(List.of(new RecoveredGeneration(failed, new TenantId(7), 8)));
        GenerationRecoveryScheduler scheduler =
                new GenerationRecoveryScheduler(generations, admission, runner, 5000);

        scheduler.recoverStaleGenerations();

        verify(admission)
                .release(
                        new ActorContext(new TenantId(7), new UserId(8), false),
                        failed);
        verify(runner).cancel("g1");
    }
}

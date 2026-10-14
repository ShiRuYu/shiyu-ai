package com.shiyu.ai.education.implementation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isNull;

import com.shiyu.ai.education.implementation.domain.port.repository.ExamTakingRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Verifies closed-page expiry across tenant boundaries. */
class ExamExpiryWorkerTest {
    @AfterEach
    void clearScope() {
        TenantScope.clear();
    }

    @Test
    void settlesEachDueAttemptUnderItsTenantAndContinuesAfterOneFailure() {
        ExamTakingRepository repository = mock(ExamTakingRepository.class);
        ExamTakingService service = mock(ExamTakingService.class);
        var first = new ExamTakingRepository.DueAttempt(new TenantId(9L), 91L);
        var second = new ExamTakingRepository.DueAttempt(new TenantId(10L), 101L);
        when(repository.findDueAttempts(any(), isNull(), eq(100))).thenReturn(List.of(first, second));
        doAnswer(invocation -> {
            assertEquals(new TenantId(9L), TenantScope.require());
            throw new IllegalStateException("one attempt failed");
        }).when(service).settleExpiredAttempt(new TenantId(9L), 91L);
        doAnswer(invocation -> {
            assertEquals(new TenantId(10L), TenantScope.require());
            return null;
        }).when(service).settleExpiredAttempt(new TenantId(10L), 101L);
        ExamExpiryWorker worker = new ExamExpiryWorker(repository, service);

        TenantScope.withTenant(new TenantId(7L), () -> {
            worker.settleDueAttempts();
            assertEquals(new TenantId(7L), TenantScope.require());
            return null;
        });

        verify(service).settleExpiredAttempt(new TenantId(9L), 91L);
        verify(service).settleExpiredAttempt(new TenantId(10L), 101L);
    }

    @Test
    void nextBatchAdvancesPastAttemptsThatKeepFailing() {
        ExamTakingRepository repository = mock(ExamTakingRepository.class);
        ExamTakingService service = mock(ExamTakingService.class);
        var failing = new ExamTakingRepository.DueAttempt(new TenantId(9L), 100L);
        var later = new ExamTakingRepository.DueAttempt(new TenantId(10L), 101L);
        when(repository.findDueAttempts(any(), isNull(), eq(100))).thenReturn(List.of(failing));
        when(repository.findDueAttempts(any(), eq(100L), eq(100))).thenReturn(List.of(later));
        doAnswer(invocation -> {
            throw new IllegalStateException("cannot settle yet");
        }).when(service).settleExpiredAttempt(new TenantId(9L), 100L);
        ExamExpiryWorker worker = new ExamExpiryWorker(repository, service);

        worker.settleDueAttempts();
        worker.settleDueAttempts();

        verify(service).settleExpiredAttempt(new TenantId(10L), 101L);
    }
}

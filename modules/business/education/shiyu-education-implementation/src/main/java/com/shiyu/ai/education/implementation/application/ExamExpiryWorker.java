package com.shiyu.ai.education.implementation.application;

import com.shiyu.ai.education.implementation.domain.port.repository.ExamTakingRepository;
import com.shiyu.ai.kernel.context.TenantScope;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 即使学生关闭页面，也会在截止时间后结算考试作答记录。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExamExpiryWorker {
    private static final int BATCH_SIZE = 100;
    private final ExamTakingRepository attempts;
    private final ExamTakingService service;
    private Long lastScannedAttemptId;

    /** 按批次处理到期作答记录；每条记录使用独立事务和租户作用域。 */
    @Scheduled(initialDelayString = "${shiyu.education.exam.expiry-initial-delay-ms:5000}",
            fixedDelayString = "${shiyu.education.exam.expiry-delay-ms:10000}")
    public synchronized void settleDueAttempts() {
        LocalDateTime now = LocalDateTime.now();
        var batch = attempts.findDueAttempts(now, lastScannedAttemptId, BATCH_SIZE);
        if (batch.isEmpty() && lastScannedAttemptId != null) {
            lastScannedAttemptId = null;
            batch = attempts.findDueAttempts(now, null, BATCH_SIZE);
        }
        for (var due : batch) {
            lastScannedAttemptId = due.attemptId();
            try {
                TenantScope.withTenant(due.tenantId(), () -> {
                    service.settleExpiredAttempt(due.tenantId(), due.attemptId());
                    return null;
                });
            } catch (RuntimeException error) {
                log.error("Failed to settle expired exam attempt: tenantId={}, attemptId={}",
                        due.tenantId().value(), due.attemptId(), error);
            }
        }
    }
}

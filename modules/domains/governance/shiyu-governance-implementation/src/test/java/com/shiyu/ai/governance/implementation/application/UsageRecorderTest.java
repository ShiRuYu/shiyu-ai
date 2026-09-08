package com.shiyu.ai.governance.implementation.application;

import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageRecordResult;
import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.governance.implementation.persistence.UsageLedger;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsageRecorderTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(12), new UserId(7), false);

    @Test
    void duplicateSourceIsAcceptedWithoutChargingTwice() {
        InMemoryUsageLedger ledger = new InMemoryUsageLedger();
        UsageRecorder recorder = new UsageRecorder(ledger);
        DomainEventEnvelope<UsageMeasurement> usage = usageFor(new TenantId(12));

        assertEquals(UsageRecordResult.RECORDED, recorder.record(ACTOR, usage));
        assertEquals(UsageRecordResult.DUPLICATE, recorder.record(ACTOR, usage));
        assertEquals(1, ledger.keys.size());
    }

    @Test
    void crossTenantUsageIsRejectedBeforePersistence() {
        InMemoryUsageLedger ledger = new InMemoryUsageLedger();
        UsageRecorder recorder = new UsageRecorder(ledger);

        assertThrows(RuntimeException.class, () -> recorder.record(ACTOR, usageFor(new TenantId(99))));
        assertEquals(0, ledger.keys.size());
    }

    @Test
    void persistenceFailureFailsTheCommand() {
        UsageLedger failingLedger = entry -> {
            throw new IllegalStateException("database unavailable");
        };
        UsageRecorder recorder = new UsageRecorder(failingLedger);

        assertThrows(IllegalStateException.class, () -> recorder.record(ACTOR, usageFor(new TenantId(12))));
    }

    @Test
    void rejectsNullArgumentsAndMismatchedUsersBeforeWriting() {
        UsageRecorder recorder = new UsageRecorder(entry -> true);
        assertThrows(NullPointerException.class, () -> recorder.record(null, usageFor(new TenantId(12))));
        assertThrows(NullPointerException.class, () -> recorder.record(ACTOR, null));
        DomainEventEnvelope<UsageMeasurement> wrongUser = new DomainEventEnvelope<>(
                ACTOR.tenantId(), new UserId(99), new CorrelationId("run-other"), Instant.now(),
                usageFor(new TenantId(12)).event());
        assertThrows(RuntimeException.class, () -> recorder.record(ACTOR, wrongUser));
    }

    private static DomainEventEnvelope<UsageMeasurement> usageFor(TenantId tenantId) {
        return new DomainEventEnvelope<>(
                tenantId,
                ACTOR.userId(),
                new CorrelationId("run-abc"),
                Instant.parse("2026-08-23T12:00:00Z"),
                new UsageMeasurement(
                        UsageSourceType.CONVERSATION_GENERATION,
                        "generation-42",
                        120,
                        30,
                        new BigDecimal("0.0042")
                )
        );
    }

    private static final class InMemoryUsageLedger implements UsageLedger {

        private final Set<String> keys = new HashSet<>();

        @Override
        public boolean insertIfAbsent(UsageLedger.Entry entry) {
            String key = entry.tenantId().value() + ":" + entry.sourceType() + ":" + entry.sourceId();
            return keys.add(key);
        }
    }
}

package com.shiyu.ai.composition.integration.governance;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.contract.model.GenerationStatus;
import com.shiyu.ai.governance.contract.QuotaDecision;
import com.shiyu.ai.governance.contract.QuotaGovernance;
import com.shiyu.ai.governance.contract.QuotaRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QuotaGenerationAdmissionTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(7), new UserId(8), false);
    private static final GenerationRun RUN =
            new GenerationRun(
                    "run-1",
                    "conversation-1",
                    null,
                    null,
                    null,
                    null,
                    GenerationStatus.CREATED,
                    0,
                    0,
                    0,
                    null,
                    -1,
                    false,
                    0,
                    Instant.EPOCH,
                    Instant.EPOCH);

    @Test
    void failedReleaseRetainsReservationForRetry() {
        QuotaGovernance quota = mock(QuotaGovernance.class);
        when(quota.reserve(ACTOR, new QuotaRequest(20, 0), RUN.id()))
                .thenReturn(new QuotaDecision(true, null, 41));
        doThrow(new IllegalStateException("temporary quota failure"))
                .doNothing()
                .when(quota)
                .release(ACTOR, RUN.id());
        QuotaGenerationAdmission admission = new QuotaGenerationAdmission(quota);
        admission.reserve(ACTOR, RUN, 20);

        assertThrows(IllegalStateException.class, () -> admission.release(ACTOR, RUN));
        admission.release(ACTOR, RUN);
        admission.release(ACTOR, RUN);

        verify(quota, times(3)).release(ACTOR, RUN.id());
    }
}

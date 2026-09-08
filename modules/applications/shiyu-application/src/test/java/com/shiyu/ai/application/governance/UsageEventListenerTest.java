package com.shiyu.ai.application.governance;

import com.shiyu.ai.model.event.EmbeddingCallEvent;
import com.shiyu.ai.model.event.ModelCallEvent;
import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UsageEventListenerTest {

    private final UsageGovernance usage = mock(UsageGovernance.class);
    private final UsageEventListener listener = new UsageEventListener(usage);

    @Test
    void rejectsModelEventsWithoutCompleteActorAttribution() {
        listener.onModelCall(new ModelCallEvent(
                "OPENAI", "gpt", 10, 4, 12, null, new TenantId(7L), null));

        verifyNoInteractions(usage);
    }

    @Test
    void recordsModelEventsWithTenantAndUserAttribution() {
        ModelCallEvent event = new ModelCallEvent(
                "OPENAI", "gpt", 10, 4, 12, null, new TenantId(7L), new UserId(11L));
        listener.onModelCall(event);

        ArgumentCaptor<DomainEventEnvelope<UsageMeasurement>> captor = ArgumentCaptor.captor();
        verify(usage).record(any(), captor.capture());
        assertEquals(event.getSourceId(), captor.getValue().event().sourceId());
        assertEquals(event.getCorrelationId(), captor.getValue().correlationId());
    }

    @Test
    void rejectsEmbeddingEventsWithoutCompleteActorAttribution() {
        listener.onEmbeddingCall(new EmbeddingCallEvent(
                "embedding", 20, 5, 1, 8, new TenantId(7L), null));

        verifyNoInteractions(usage);
    }
}

package com.shiyu.ai.governance.implementation.usage.service;

import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageRecordResult;
import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.governance.implementation.usage.realtime.UsageRealtimePublisher;
import com.shiyu.ai.governance.implementation.usage.port.BillingPriceProvider;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

class UsageRecordServiceTest {

    private final UsageRecordRepository repository = mock(UsageRecordRepository.class);
    private final UsageRecordService service = new UsageRecordService(repository);

    @Test
    void rejectsUsageWithoutAnExplicitTenant() {
        assertThrows(NullPointerException.class, () -> service.recordUsage(
                "OPENAI", "gpt", 1, 2, 3, null, 7L, "conversation", "run-1"));
    }

    @Test
    void rejectsUsageWithoutAnExplicitUser() {
        assertThrows(NullPointerException.class, () -> service.recordUsage(
                "OPENAI", "gpt", 1, 2, 3, new TenantId(9L), null, "conversation", "run-1"));
    }

    @Test
    void propagatesCriticalPersistenceFailures() {
        IllegalStateException failure = new IllegalStateException("database unavailable");
        doThrow(failure).when(repository).insert(any());

        assertThrows(IllegalStateException.class, () -> service.recordUsage(
                "OPENAI", "gpt", 1, 2, 3, new TenantId(9L), 7L, "conversation", "run-1"));
    }

    @Test
    void rejectsEmbeddingUsageWithoutAnExplicitActor() {
        assertThrows(NullPointerException.class, () -> service.recordEmbedding(
                "embedding", 10, 3, 1, 2, null, 7L, "document-1"));
        assertThrows(NullPointerException.class, () -> service.recordEmbedding(
                "embedding", 10, 3, 1, 2, new TenantId(9L), null, "document-1"));
    }

    @Test
    void recordsContractUsageWithExplicitOwnershipAndSourceIdentity() {
        when(repository.insertIfAbsent(any())).thenReturn(true);
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.MODEL_INVOCATION, "model-call-1", 4, 6,
                new BigDecimal("0.12"), 15,
                Map.of("usageType", "LLM", "platform", "OPENAI", "model", "gpt"));
        DomainEventEnvelope<UsageMeasurement> envelope = new DomainEventEnvelope<>(
                actor.tenantId(), actor.userId(), new CorrelationId("corr-1"),
                Instant.parse("2026-08-24T04:00:00Z"), measurement);

        assertEquals(UsageRecordResult.RECORDED, service.record(actor, envelope));

        var captured = org.mockito.ArgumentCaptor.forClass(UsageRecordBO.class);
        verify(repository).insertIfAbsent(captured.capture());
        UsageRecordBO record = captured.getValue();
        assertEquals(9L, record.getTenantId());
        assertEquals(7L, record.getUserId());
        assertEquals("MODEL_INVOCATION", record.getSourceType());
        assertEquals("model-call-1", record.getSourceId());
        assertEquals("corr-1", record.getCorrelationId());
        assertEquals(4L, record.getInputTokens());
        assertEquals(6L, record.getOutputTokens());
    }

    @Test
    void duplicateContractEventsDoNotPublishOrChargeAgain() {
        when(repository.insertIfAbsent(any())).thenReturn(false);
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        service.setRealtimePublisher(publisher);
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.MODEL_INVOCATION, "same-run", 4, 6,
                new BigDecimal("0.12"), 15,
                Map.of("usageType", "LLM", "platform", "OPENAI", "model", "gpt"));
        DomainEventEnvelope<UsageMeasurement> envelope = new DomainEventEnvelope<>(
                actor.tenantId(), actor.userId(), new CorrelationId("corr-1"),
                Instant.parse("2026-08-24T04:00:00Z"), measurement);

        assertEquals(UsageRecordResult.DUPLICATE, service.record(actor, envelope));
        verify(publisher, never()).publishUsageRecord(any(), any(), any(Integer.class), any(Integer.class),
                any(Long.class), any(Double.class));
    }

    @Test
    void rejectsActorMismatchAndTenantMismatch() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.MODEL_INVOCATION, "run-1", 1, 1,
                BigDecimal.ZERO, 1, Map.of());
        DomainEventEnvelope<UsageMeasurement> wrongUser = new DomainEventEnvelope<>(
                actor.tenantId(), new UserId(8), new CorrelationId("corr"), Instant.now(), measurement);
        assertThrows(RuntimeException.class, () -> service.record(actor, wrongUser));

        DomainEventEnvelope<UsageMeasurement> wrongTenant = new DomainEventEnvelope<>(
                new TenantId(8), actor.userId(), new CorrelationId("corr"), Instant.now(), measurement);
        assertThrows(RuntimeException.class, () -> service.record(actor, wrongTenant));
    }

    @Test
    void toleratesRealtimeFailuresWhilePreservingCriticalPersistence() {
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("socket down"))
                .when(publisher).publishUsageRecord(any(), any(), any(Integer.class), any(Integer.class),
                        any(Long.class), any(Double.class));
        service.setRealtimePublisher(publisher);
        service.recordUsage("OPENAI", "gpt", 2, 3, 5, new TenantId(9L), 7L, "session", "run-1");
        verify(repository).insert(any(UsageRecordBO.class));
    }

    @Test
    void recordsEmbeddingContractUsageAndPublishesOnlyAfterInsert() {
        when(repository.insertIfAbsent(any())).thenReturn(true);
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        service.setRealtimePublisher(publisher);
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.KNOWLEDGE_INDEXING, "embedding-1", 12, 0,
                BigDecimal.ZERO, 22,
                Map.of("usageType", "EMBEDDING", "model", "embed", "textLength", "42", "vectorCount", "3"));
        DomainEventEnvelope<UsageMeasurement> envelope = new DomainEventEnvelope<>(
                actor.tenantId(), actor.userId(), new CorrelationId("corr-embedding"),
                Instant.parse("2026-08-24T04:00:00Z"), measurement);

        assertEquals(UsageRecordResult.RECORDED, service.record(actor, envelope));
        verify(publisher).publishEmbeddingUsage("embed", 42, 12, 3, 22);
    }

    @Test
    void skipsRealtimePublicationForDuplicateEmbeddingContractUsage() {
        when(repository.insertIfAbsent(any())).thenReturn(false);
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        service.setRealtimePublisher(publisher);
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.KNOWLEDGE_INDEXING, "embedding-duplicate", 12, 0,
                BigDecimal.ZERO, 22, Map.of("usageType", "EMBEDDING"));
        DomainEventEnvelope<UsageMeasurement> envelope = new DomainEventEnvelope<>(
                actor.tenantId(), actor.userId(), new CorrelationId("corr-embedding"), Instant.now(), measurement);

        assertEquals(UsageRecordResult.DUPLICATE, service.record(actor, envelope));
        verifyNoInteractions(publisher);
    }

    @Test
    void usesExternalPricingAndGenerationRunAsTheStableSourceIdentity() {
        BillingPriceProvider pricing = mock(BillingPriceProvider.class);
        when(pricing.price("OPENAI", "gpt")).thenReturn(
                new BillingPriceProvider.PriceSnapshot("OPENAI", "gpt", BigDecimal.valueOf(0.01), BigDecimal.valueOf(0.02), "price-v2"));
        service.setBillingPriceProvider(pricing);
        service.recordUsage("OPENAI", "gpt", 3, 4, 5, new TenantId(9L), 7L, "session", "generation-1");

        var captured = org.mockito.ArgumentCaptor.forClass(UsageRecordBO.class);
        verify(repository).insert(captured.capture());
        UsageRecordBO record = captured.getValue();
        assertEquals(9L, record.getTenantId());
        assertEquals(7L, record.getUserId());
        assertEquals("GENERATION_RUN", record.getSourceType());
        assertEquals("generation-1", record.getSourceId());
        assertEquals(new BigDecimal("0.11"), record.getCost());
        assertNotNull(record.getExtInfo());
    }

    @Test
    void rejectsNullContractArgumentsBeforeTouchingPersistence() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        assertThrows(NullPointerException.class, () -> service.record(null, null));
        assertThrows(NullPointerException.class, () -> service.record(actor, null));
        assertThrows(NullPointerException.class, () -> service.record(null,
                new DomainEventEnvelope<>(actor.tenantId(), actor.userId(), new CorrelationId("corr"), Instant.now(),
                        new UsageMeasurement(UsageSourceType.MODEL_INVOCATION, "run", 1, 1, BigDecimal.ZERO, 1, Map.of()))));
    }

    @Test
    void handlesMalformedOptionalRealtimeValuesWithoutOverflow() {
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        service.setRealtimePublisher(publisher);
        service.recordEmbedding("embed", 10, 3, Integer.MAX_VALUE, 2, new TenantId(9L), 7L, "session");
        verify(publisher).publishEmbeddingUsage("embed", 10, 3, Integer.MAX_VALUE, 2);
    }

    @Test
    void usesStableFallbacksForBlankGenerationIdsAndMalformedEmbeddingAttributes() {
        service.registerPricing(new com.shiyu.ai.governance.implementation.usage.model.ModelPricing(
                "LOCAL", "model", 0.1, 0.2));
        assertEquals(2, service.getPricingCount());
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        service.setRealtimePublisher(publisher);
        service.recordUsage("LOCAL", "model", 1, 2, 3, new TenantId(9L), 7L, "session", " ");
        verify(repository).insert(any(UsageRecordBO.class));

        when(repository.insertIfAbsent(any())).thenReturn(true);
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.KNOWLEDGE_INDEXING, "embedding-bad", Long.MAX_VALUE, 0,
                BigDecimal.ZERO, 2,
                Map.of("usageType", "EMBEDDING", "model", "embed", "textLength", "bad", "vectorCount", "999999999999999999999"));
        DomainEventEnvelope<UsageMeasurement> envelope = new DomainEventEnvelope<>(
                actor.tenantId(), actor.userId(), new CorrelationId("corr-bad"), Instant.now(), measurement);
        service.record(actor, envelope);
        verify(publisher).publishEmbeddingUsage("embed", 0, Integer.MAX_VALUE, 0, 2);
    }

    @Test
    void preservesCriticalEmbeddingPersistenceWhenRealtimePushFails() {
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        doThrow(new IllegalStateException("socket down")).when(publisher)
                .publishEmbeddingUsage(any(), any(Integer.class), any(Integer.class), any(Integer.class), any(Long.class));
        service.setRealtimePublisher(publisher);
        service.recordEmbedding("embed", 10, 3, 1, 2, new TenantId(9L), 7L, "document-1");
        verify(repository).insert(any(UsageRecordBO.class));
    }
}

package com.shiyu.ai.governance.web;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsageWebSocketServiceTest {

    @Test
    void publishesAllUsagePayloadKindsOnlyWhenAClientIsConnected() {
        UsageWebSocketHandler handler = mock(UsageWebSocketHandler.class);
        when(handler.getActiveSessionCount()).thenReturn(1);
        UsageWebSocketService service = new UsageWebSocketService(handler);

        service.publishUsageRecord("OPENAI", "gpt", 2, 3, 9, 0.12);
        service.publishEmbeddingUsage("embed", 10, 4, 2, 5);
        service.pushDailyAggregate(7);

        verify(handler).broadcast(argThat(payload -> payload.contains("\"type\":\"USAGE_RECORD\"")));
        verify(handler).broadcast(argThat(payload -> payload.contains("\"type\":\"EMBEDDING_USAGE_RECORD\"")));
        verify(handler).broadcast(argThat(payload -> payload.contains("\"type\":\"USAGE_DAILY_AGGREGATE\"")));
    }

    @Test
    void skipsPayloadsForEmptySessionsAndToleratesBroadcastFailures() {
        UsageWebSocketHandler handler = mock(UsageWebSocketHandler.class);
        when(handler.getActiveSessionCount()).thenReturn(0);
        UsageWebSocketService service = new UsageWebSocketService(handler);
        service.publishUsageRecord("OPENAI", "gpt", 1, 1, 1, 0.01);
        service.publishEmbeddingUsage("embed", 1, 1, 1, 1);
        service.pushDailyAggregate(1);
        verify(handler, never()).broadcast(org.mockito.ArgumentMatchers.anyString());

        when(handler.getActiveSessionCount()).thenReturn(1);
        doThrow(new RuntimeException("connection lost"))
                .when(handler).broadcast(org.mockito.ArgumentMatchers.anyString());
        service.publishUsageRecord("OPENAI", "gpt", 1, 1, 1, 0.01);
        service.publishEmbeddingUsage("embed", 1, 1, 1, 1);
        service.pushDailyAggregate(1);
    }
}

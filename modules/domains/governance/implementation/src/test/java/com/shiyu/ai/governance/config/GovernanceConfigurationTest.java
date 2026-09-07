package com.shiyu.ai.governance.config;

import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.implementation.usage.config.UsageAutoConfiguration;
import com.shiyu.ai.governance.implementation.usage.persistence.UsagePersistenceConfiguration;
import com.shiyu.ai.governance.implementation.usage.port.BillingPriceProvider;
import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.governance.implementation.usage.realtime.UsageRealtimePublisher;
import com.shiyu.ai.governance.implementation.usage.service.UsageRecordService;
import com.shiyu.ai.governance.web.UsageWebSocketConfig;
import com.shiyu.ai.governance.web.UsageWebSocketHandler;
import com.shiyu.ai.governance.web.UsageWebSocketService;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

class GovernanceConfigurationTest {

    @Test
    void wiresUsageRecordServiceWithOptionalAdapters() {
        UsageAutoConfiguration configuration = new UsageAutoConfiguration();
        UsageRecordRepository repository = mock(UsageRecordRepository.class);
        UsageRealtimePublisher publisher = mock(UsageRealtimePublisher.class);
        BillingPriceProvider pricing = mock(BillingPriceProvider.class);

        UsageRecordService service = configuration.usageRecordService(repository, publisher, pricing);
        assertNotNull(service);
        assertEquals(1, service.getPricingCount());
        assertSame(service, configuration.usageGovernance(service));
        assertNotNull(configuration.usageRecordService(repository, null, null));
    }

    @Test
    void exposesWebSocketBeansAndRegistersTheUsageEndpoint() {
        UsageWebSocketConfig configuration = new UsageWebSocketConfig();
        UsageWebSocketHandler handler = configuration.usageWebSocketHandler();
        UsageWebSocketService service = configuration.usageWebSocketService(handler);
        assertNotNull(handler);
        assertNotNull(service);

        WebSocketHandlerRegistry registry = mock(WebSocketHandlerRegistry.class);
        WebSocketHandlerRegistration registration = mock(WebSocketHandlerRegistration.class);
        doReturn(registration).when(registry).addHandler(any(UsageWebSocketHandler.class), any(String[].class));
        when(registration.setAllowedOriginPatterns(any(String[].class))).thenReturn(registration);
        configuration.registerWebSocketHandlers(registry);
    }

    @Test
    void canInstantiatePersistenceConfiguration() {
        assertNotNull(new UsagePersistenceConfiguration());
    }
}

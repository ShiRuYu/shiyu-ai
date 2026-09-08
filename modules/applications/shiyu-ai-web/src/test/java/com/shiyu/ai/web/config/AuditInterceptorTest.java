package com.shiyu.ai.web.config;

import com.shiyu.ai.agent.service.AuditService;
import com.shiyu.ai.common.web.auth.ClientIpResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@Tag("dev")
class AuditInterceptorTest {

    @Test
    void recordsResolvedClientIpInsteadOfTransportAddress() throws Exception {
        AuditService auditService = mock(AuditService.class);
        ClientIpResolver clientIpResolver = mock(ClientIpResolver.class);
        when(clientIpResolver.currentClientIp()).thenReturn("203.0.113.7");
        AuditInterceptor interceptor = new AuditInterceptor(auditService, clientIpResolver);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/agent/agents");
        request.setRemoteAddr("192.0.2.8");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(clientIpResolver).currentClientIp();
        verify(auditService).record(
                nullable(com.shiyu.ai.kernel.context.TenantId.class), nullable(Long.class),
                eq("203.0.113.7"), eq("AGENT_GET"), eq("agent"), isNull(), anyMap(),
                eq("SUCCESS"), isNull(), anyLong());
    }

    @Test
    void skipsHealthChecksWithoutResolvingActorOrClientIp() throws Exception {
        AuditService auditService = mock(AuditService.class);
        ClientIpResolver clientIpResolver = mock(ClientIpResolver.class);
        AuditInterceptor interceptor = new AuditInterceptor(auditService, clientIpResolver);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        verifyNoInteractions(auditService, clientIpResolver);
    }

    @Test
    void containsAuditStorageFailures() throws Exception {
        AuditService auditService = mock(AuditService.class);
        ClientIpResolver clientIpResolver = mock(ClientIpResolver.class);
        when(clientIpResolver.currentClientIp()).thenReturn("192.0.2.10");
        doThrow(new IllegalStateException("audit store unavailable"))
                .when(auditService)
                .record(nullable(com.shiyu.ai.kernel.context.TenantId.class), nullable(Long.class),
                        eq("192.0.2.10"), eq("API_GET"), eq("api"), isNull(), anyMap(),
                        eq("SUCCESS"), isNull(), anyLong());
        AuditInterceptor interceptor = new AuditInterceptor(auditService, clientIpResolver);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/unknown");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        assertDoesNotThrow(() -> interceptor.afterCompletion(request, response, new Object(), null));
    }
}

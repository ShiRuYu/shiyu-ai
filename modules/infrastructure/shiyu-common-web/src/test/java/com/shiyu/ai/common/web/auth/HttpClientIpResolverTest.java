package com.shiyu.ai.common.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpClientIpResolverTest {

    @Test
    void prefersTheFirstForwardedAddress() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(" 203.0.113.10, 198.51.100.4");

        assertEquals("203.0.113.10", new HttpClientIpResolver(request).currentClientIp());
    }

    @Test
    void fallsBackThroughProxyHeadersAndRemoteAddress() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn(" ");
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.0.2.20");

        assertEquals("192.0.2.20", new HttpClientIpResolver(request).currentClientIp());
    }

    @Test
    void returnsUnknownWhenNoAddressIsAvailable() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertEquals("unknown", new HttpClientIpResolver(request).currentClientIp());
    }
}

package com.shiyu.ai.web.interceptor;

import com.shiyu.ai.auth.service.AuthContextService;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Tag("dev")
class UserContextInterceptorTest {

    @Test
    void skipsNonInitialDispatcherTypesBeforeUsingThreadLocalAuthentication() throws Exception {
        AuthContextService authContextService = mock(AuthContextService.class);
        UserContextInterceptor interceptor = new UserContextInterceptor(authContextService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verifyNoInteractions(authContextService, response);
    }

    @Test
    void clearsTenantScopeAfterRequestCompletion() {
        TenantScope.set(new TenantId(42L));
        UserContextInterceptor interceptor = new UserContextInterceptor(mock(AuthContextService.class));

        interceptor.afterCompletion(mock(HttpServletRequest.class), mock(HttpServletResponse.class),
                new Object(), null);

        assertTrue(TenantScope.current().isEmpty());
    }

    @Test
    void unauthenticatedRequestsUseHttpUnauthorizedStatus() throws Exception {
        AuthContextService authContextService = mock(AuthContextService.class);
        UserContextInterceptor interceptor = new UserContextInterceptor(authContextService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(request.getMethod()).thenReturn("GET");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(request.getRequestURI()).thenReturn("/api/agent/definitions");
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.isFrameworkLogin()).thenReturn(false);

        try (var mocked = org.mockito.Mockito.mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);

            assertTrue(!interceptor.preHandle(request, response, new Object()));
        }

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
}

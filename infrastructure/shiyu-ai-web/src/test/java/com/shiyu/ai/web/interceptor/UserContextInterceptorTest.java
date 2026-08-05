package com.shiyu.ai.web.interceptor;

import com.shiyu.ai.auth.service.AuthContextService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
}

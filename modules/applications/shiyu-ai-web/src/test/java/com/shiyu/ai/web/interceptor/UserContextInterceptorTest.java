package com.shiyu.ai.web.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.web.auth.ClientIpResolver;
import com.shiyu.ai.iam.implementation.api.response.AuthRoleResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthScopeRoleResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthTenantResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthUserResponse;
import com.shiyu.ai.iam.implementation.service.AuthContextService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

@Tag("dev")
class UserContextInterceptorTest {

    @Test
    void skipsNonInitialDispatcherTypesBeforeUsingThreadLocalAuthentication() throws Exception {
        AuthContextService authContextService = mock(AuthContextService.class);
        UserContextInterceptor interceptor =
                new UserContextInterceptor(authContextService, mock(ClientIpResolver.class));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verifyNoInteractions(authContextService, response);
    }

    @Test
    void clearsTenantScopeAfterRequestCompletion() {
        TenantScope.set(new TenantId(42L));
        UserContextInterceptor interceptor =
                new UserContextInterceptor(
                        mock(AuthContextService.class), mock(ClientIpResolver.class));

        interceptor.afterCompletion(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                new Object(),
                null);

        assertTrue(TenantScope.current().isEmpty());
    }

    @Test
    void unauthenticatedRequestsUseHttpUnauthorizedStatus() throws Exception {
        AuthContextService authContextService = mock(AuthContextService.class);
        UserContextInterceptor interceptor =
                new UserContextInterceptor(authContextService, mock(ClientIpResolver.class));
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

    @Test
    void usesUnifiedClientIpResolverForCachedUserContext() throws Exception {
        AuthContextService authContextService = mock(AuthContextService.class);
        ClientIpResolver clientIpResolver = mock(ClientIpResolver.class);
        when(clientIpResolver.currentClientIp()).thenReturn("203.0.113.7");

        AuthUserResponse user = new AuthUserResponse();
        user.setId(42L);
        user.setStatus(1);
        user.setDelFlag(0);
        AuthTenantResponse tenant = new AuthTenantResponse();
        tenant.setId(7L);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        AuthScopeRoleResponse assignment = new AuthScopeRoleResponse();
        assignment.setUserId(42L);
        assignment.setTenantId(7L);
        assignment.setRoleId(9L);
        assignment.setStatus(1);
        assignment.setDelFlag(0);
        AuthRoleResponse role = new AuthRoleResponse();
        role.setId(9L);
        role.setCode("member");
        role.setStatus(1);
        role.setDelFlag(0);
        when(authContextService.user(42L)).thenReturn(user);
        when(authContextService.tenant(7L)).thenReturn(tenant);
        when(authContextService.scopeRoles(42L)).thenReturn(java.util.List.of(assignment));
        when(authContextService.role(9L)).thenReturn(role);

        UserContext cachedContext = new UserContext();
        cachedContext.setUserId(42L);
        cachedContext.setHomeTenantId(7L);
        cachedContext.setCurrentTenantId(7L);
        cachedContext.setCurrentRoleId(9L);
        cachedContext.setCurrentRoleCode("member");
        UserContextInterceptor interceptor =
                new UserContextInterceptor(authContextService, clientIpResolver);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);

        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.isFrameworkLogin()).thenReturn(true);
        try (var mocked = org.mockito.Mockito.mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            mocked.when(SaTokenHelper::getCurrentUserId).thenReturn(42L);
            mocked.when(SaTokenHelper::getUserContextFromSession).thenReturn(cachedContext);
            mocked.when(SaTokenHelper::getCurrentToken).thenReturn("token");

            assertTrue(interceptor.preHandle(request, response, new Object()));
        }

        assertEquals("203.0.113.7", cachedContext.getIpaddr());
        verify(clientIpResolver).currentClientIp();
        interceptor.afterCompletion(request, response, new Object(), null);
    }
}

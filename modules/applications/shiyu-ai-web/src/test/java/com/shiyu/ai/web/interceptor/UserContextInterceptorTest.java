package com.shiyu.ai.web.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.context.model.UserContext;
import com.shiyu.ai.common.foundation.context.UserContextHolder;
import com.shiyu.ai.common.web.auth.ClientIpResolver;
import com.shiyu.ai.iam.implementation.api.response.AuthRoleResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthScopeRoleResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthTenantResponse;
import com.shiyu.ai.iam.implementation.api.response.AuthUserResponse;
import com.shiyu.ai.iam.implementation.application.service.AuthContextService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 验证 用户 Context Interceptor 相关功能、边界条件、异常路径和协作行为。
 */
@Tag("dev")
class UserContextInterceptorTest {

    @Test
    void delegatedChildRoleRequiresAnOwnedHomeSuperRole() throws Exception {
        AuthContextService authContextService = mock(AuthContextService.class);
        AuthUserResponse user = new AuthUserResponse();
        user.setId(42L);
        user.setStatus(1);
        user.setDelFlag(0);
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":20,"
                + "\"switchMode\":\"PARENT_SUPER_ADMIN\"}");
        AuthTenantResponse tenant = new AuthTenantResponse();
        tenant.setId(20L);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        AuthScopeRoleResponse assignment = new AuthScopeRoleResponse();
        assignment.setUserId(42L);
        assignment.setTenantId(10L);
        assignment.setRoleId(7L);
        assignment.setStatus(1);
        assignment.setDelFlag(0);
        AuthRoleResponse homeSuper = new AuthRoleResponse();
        homeSuper.setId(7L);
        homeSuper.setTenantId(10L);
        homeSuper.setCode("tenant_super");
        homeSuper.setStatus(1);
        homeSuper.setDelFlag(0);
        AuthRoleResponse childSuper = new AuthRoleResponse();
        childSuper.setId(8L);
        childSuper.setTenantId(20L);
        childSuper.setCode("tenant_super");
        childSuper.setStatus(1);
        childSuper.setDelFlag(0);
        when(authContextService.user(42L)).thenReturn(user);
        when(authContextService.tenant(20L)).thenReturn(tenant);
        when(authContextService.scopeRoles(42L)).thenReturn(java.util.List.of(assignment));
        when(authContextService.role(7L)).thenReturn(homeSuper);
        when(authContextService.descendantTenantIds(new TenantId(10L)))
                .thenReturn(java.util.List.of(10L, 20L));
        when(authContextService.tenantSuperRole(new TenantId(20L))).thenReturn(childSuper);
        UserContextInterceptor interceptor = new UserContextInterceptor(
                authContextService, mock(ClientIpResolver.class));
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(request.getMethod()).thenReturn("GET");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.isFrameworkLogin()).thenReturn(true);
        try (var mocked = org.mockito.Mockito.mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            mocked.when(SaTokenHelper::getCurrentUserId).thenReturn(42L);
            mocked.when(SaTokenHelper::getCurrentToken).thenReturn("token");
            assertTrue(interceptor.preHandle(request, response, new Object()));
            assertEquals("PARENT_SUPER_ADMIN", UserContextHolder.getSwitchMode());
            assertEquals(8L, UserContextHolder.getCurrentRoleId());
            interceptor.afterCompletion(request, response, new Object(), null);
            homeSuper.setTenantId(99L);
            MockHttpServletResponse denied = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(request, denied, new Object()));
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, denied.getStatus());
        } finally {
            interceptor.afterCompletion(request, response, new Object(), null);
        }
    }

    @Test
    void staleDelegatedMarkerIsNotGrantedForAnOrdinaryAssignedTenant() throws Exception {
        AuthContextService authContextService = mock(AuthContextService.class);
        AuthUserResponse user = new AuthUserResponse();
        user.setId(42L);
        user.setStatus(1);
        user.setDelFlag(0);
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":20,"
                + "\"switchMode\":\"PARENT_SUPER_ADMIN\","
                + "\"currentRole\":{\"roleId\":9,\"roleKey\":\"member\"}}");
        AuthTenantResponse tenant = new AuthTenantResponse();
        tenant.setId(20L);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        AuthScopeRoleResponse assignment = new AuthScopeRoleResponse();
        assignment.setUserId(42L);
        assignment.setTenantId(20L);
        assignment.setRoleId(9L);
        assignment.setStatus(1);
        assignment.setDelFlag(0);
        AuthRoleResponse role = new AuthRoleResponse();
        role.setId(9L);
        role.setTenantId(20L);
        role.setCode("member");
        role.setStatus(1);
        role.setDelFlag(0);
        when(authContextService.user(42L)).thenReturn(user);
        when(authContextService.tenant(20L)).thenReturn(tenant);
        when(authContextService.scopeRoles(42L)).thenReturn(java.util.List.of(assignment));
        when(authContextService.role(9L)).thenReturn(role);
        UserContextInterceptor interceptor = new UserContextInterceptor(
                authContextService, mock(ClientIpResolver.class));
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(request.getMethod()).thenReturn("GET");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.isFrameworkLogin()).thenReturn(true);
        try (var mocked = org.mockito.Mockito.mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            mocked.when(SaTokenHelper::getCurrentUserId).thenReturn(42L);
            mocked.when(SaTokenHelper::getCurrentToken).thenReturn("token");
            assertTrue(interceptor.preHandle(request, response, new Object()));
            assertEquals("NORMAL", UserContextHolder.getSwitchMode());
            assertFalse(UserContextHolder.getContext().isParentSuperAdminSwitch());
            interceptor.afterCompletion(request, response, new Object(), null);
            user.setStatus(0);
            MockHttpServletResponse denied = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(request, denied, new Object()));
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, denied.getStatus());
        } finally {
            interceptor.afterCompletion(request, response, new Object(), null);
        }
    }

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
    void clearsTenantScopeWhenRequestHandsOffToAsyncProcessing() {
        TenantScope.set(new TenantId(42L));
        UserContextInterceptor interceptor = new UserContextInterceptor(
                mock(AuthContextService.class), mock(ClientIpResolver.class));

        interceptor.afterConcurrentHandlingStarted(
                mock(HttpServletRequest.class), mock(HttpServletResponse.class), new Object());

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
        when(request.getRequestURI()).thenReturn("/api/agent/agents/definitions");
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.isFrameworkLogin()).thenReturn(false);

        try (var mocked = org.mockito.Mockito.mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);

            assertTrue(!interceptor.preHandle(request, response, new Object()));
        }

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void newRequestClearsTenantScopeLeftByPreviousAsyncRequest() throws Exception {
        UserContextInterceptor interceptor = new UserContextInterceptor(
                mock(AuthContextService.class), mock(ClientIpResolver.class));
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(request.getMethod()).thenReturn("GET");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.isFrameworkLogin()).thenReturn(false);
        TenantScope.set(new TenantId(99L));
        try (var mocked = org.mockito.Mockito.mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertFalse(interceptor.preHandle(request, response, new Object()));
            assertTrue(TenantScope.current().isEmpty());
        } finally {
            TenantScope.clear();
        }
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
        role.setTenantId(7L);
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

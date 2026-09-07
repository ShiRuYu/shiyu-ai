package com.shiyu.ai.auth.web;

import com.shiyu.ai.auth.handler.LoginRateLimiter;
import com.shiyu.ai.auth.request.*;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.knowledge.contract.KnowledgeTenantProvisioning;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerCoverageTest {
    private final AuthService auth = mock(AuthService.class);
    private final UserService users = mock(UserService.class);
    private final LoginRateLimiter limiter = mock(LoginRateLimiter.class);
    private final KnowledgeTenantProvisioning knowledge = mock(KnowledgeTenantProvisioning.class);
    private final AuthController controller = new AuthController(auth, users, limiter, knowledge);

    @Test
    void loginValidatesRateLimitCredentialsAndResponseBranches() {
        when(limiter.getClientIp()).thenReturn("10.0.0.1");
        LoginRequest request = new LoginRequest();
        request.setUsername(" alice ");
        request.setPassword("secret");
        when(limiter.isAllowed("10.0.0.1")).thenReturn(false);
        assertFalse(controller.login(request).isSuccess());

        when(limiter.isAllowed("10.0.0.1")).thenReturn(true);
        request.setUsername(" ");
        assertFalse(controller.login(request).isSuccess());
        request.setUsername("alice");
        request.setPassword(null);
        assertFalse(controller.login(request).isSuccess());

        request.setPassword("secret");
        when(auth.login("alice", "secret", null, "10.0.0.1")).thenReturn(null);
        assertFalse(controller.login(request).isSuccess());

        LoginResponseVO response = new LoginResponseVO();
        response.setId(9L);
        response.setUsername("alice");
        response.setCurrentTenantId(7L);
        when(auth.login("alice", "secret", null, "10.0.0.1")).thenReturn(response);
        Result<LoginResponseVO> result = controller.login(request);
        assertTrue(result.isSuccess());
        verify(limiter).reset("10.0.0.1");
        verify(knowledge).initializeTenantDefaults(new TenantId(7L));
    }

    @Test
    void authMutationAndTokenEndpointsMapSuccessAndFailures() {
        LoginRequest login = new LoginRequest();
        login.setUsername("alice"); login.setPassword("secret"); login.setEmail("a@example.com");
        when(auth.register("alice", "secret", "a@example.com")).thenReturn(new LoginResponseVO());
        assertTrue(controller.register(login).isSuccess());
        when(auth.register(anyString(), anyString(), anyString())).thenThrow(new IllegalArgumentException("duplicate"));
        assertFalse(controller.register(login).isSuccess());

        CodeLoginRequest code = new CodeLoginRequest();
        code.setPhone("138"); code.setCode("1234"); code.setCaptchaKey("k");
        when(auth.codeLogin("138", "1234", "k")).thenReturn(new LoginResponseVO());
        assertTrue(controller.codeLogin(code).isSuccess());
        doThrow(new IllegalStateException("bad code"))
                .when(auth).codeLogin(anyString(), anyString(), anyString());
        assertFalse(controller.codeLogin(code).isSuccess());
        code.setPhone("139");
        doThrow(new IllegalStateException("database password=secret"))
                .when(auth).codeLogin(anyString(), anyString(), anyString());
        Result<LoginResponseVO> safeCodeFailure = controller.codeLogin(code);
        assertFalse(safeCodeFailure.isSuccess());
        assertFalse(safeCodeFailure.getMessage().contains("secret"));

        ForgetPasswordRequest forget = new ForgetPasswordRequest();
        forget.setEmail("a@example.com"); forget.setNewPassword("new"); forget.setCode("1234"); forget.setCaptchaKey("k");
        when(auth.forgetPassword("a@example.com", "new", "1234", "k")).thenReturn(true);
        assertTrue(controller.forgetPassword(forget).isSuccess());
        when(auth.forgetPassword(anyString(), anyString(), anyString(), anyString())).thenThrow(new IllegalArgumentException("invalid"));
        assertFalse(controller.forgetPassword(forget).isSuccess());

        RefreshTokenRequest refresh = new RefreshTokenRequest();
        refresh.setAccessToken("old");
        when(auth.refreshToken("old")).thenReturn("new");
        assertTrue(controller.refreshToken(refresh).isSuccess());
        when(auth.refreshToken("old")).thenReturn(null);
        assertFalse(controller.refreshToken(refresh).isSuccess());
        when(auth.refreshToken("old")).thenThrow(new IllegalStateException("expired"));
        assertFalse(controller.refreshToken(refresh).isSuccess());
    }

    @Test
    void contextEndpointsUseActorAndHandleSwitchesAndLogout() {
        ActorContext actor = new ActorContext(new TenantId(7L), new com.shiyu.ai.kernel.context.UserId(9L), false);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            ignored.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            ignored.when(ActorContextHttpAdapter::tenantId).thenReturn(7L);
            when(auth.getAuthCodesByUserId(actor, new UserId(9L))).thenReturn(List.of("agent.read"));
            assertEquals(List.of("agent.read"), controller.getAuthCodes().getData());
            when(auth.getUserTenants(actor, 9L)).thenReturn(List.of());
            assertTrue(controller.getUserTenants().isSuccess());

            SwitchRoleRequest role = new SwitchRoleRequest(); role.setRoleId(3L);
            when(auth.switchCurrentRole(9L, 3L)).thenReturn(false);
            assertFalse(controller.switchCurrentRole(role).isSuccess());
            when(auth.switchCurrentRole(9L, 3L)).thenReturn(true);
            UserVO switchContext = new UserVO();
            switchContext.setExtInfo("{\"currentTenantId\":8,\"homeTenantId\":7,\"switchMode\":\"CHILD\"}");
            when(users.detailView(actor, 9L)).thenReturn(switchContext);
            assertTrue(controller.switchCurrentRole(role).isSuccess());
            assertEquals(8L, switchContext.getCurrentTenantId());
            assertEquals(7L, switchContext.getHomeTenantId());
            assertEquals("CHILD", switchContext.getSwitchMode());
            switchContext.setExtInfo("not-json");
            assertTrue(controller.switchCurrentRole(role).isSuccess());
            when(users.detailView(actor, 9L)).thenReturn(null);
            assertTrue(controller.switchCurrentRole(role).isSuccess());

            SwitchTenantRequest missingTenant = new SwitchTenantRequest();
            when(auth.switchCurrentTenant(9L, null)).thenReturn(false);
            assertFalse(controller.switchTenant(missingTenant).isSuccess());

            SwitchTenantRequest tenant = new SwitchTenantRequest(); tenant.setTenantId(8L);
            when(auth.switchCurrentTenant(9L, new TenantId(8L))).thenReturn(false);
            assertFalse(controller.switchTenant(tenant).isSuccess());
            when(auth.switchCurrentTenant(9L, new TenantId(8L))).thenReturn(true);
            assertTrue(controller.switchTenant(tenant).isSuccess());

            assertTrue(controller.logout("Bearer token").isSuccess());
            verify(auth).logout("token");
            assertTrue(controller.logout("raw-token").isSuccess());
            assertTrue(controller.logout(null).isSuccess());
        }
    }

    @Test
    void mapsUnexpectedFailuresAndTenantlessLogin() {
        when(limiter.getClientIp()).thenReturn("10.0.0.2");
        when(limiter.isAllowed("10.0.0.2")).thenReturn(true);
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("secret");
        when(auth.login("alice", "secret", null, "10.0.0.2")).thenThrow(new IllegalStateException("db"));
        assertFalse(controller.login(request).isSuccess());

        LoginResponseVO tenantless = new LoginResponseVO();
        tenantless.setId(9L);
        tenantless.setUsername("alice");
        reset(auth);
        when(auth.login("alice", "secret", null, "10.0.0.2")).thenReturn(tenantless);
        assertTrue(controller.login(request).isSuccess());
        verify(knowledge, never()).initializeTenantDefaults(any(TenantId.class));

        reset(auth);
        when(auth.getAuthCodesByUserId(any(), any(UserId.class))).thenThrow(new IllegalStateException("lookup"));
        try (var mocked = mockStatic(ActorContextHttpAdapter.class)) {
            mocked.when(ActorContextHttpAdapter::currentActor).thenReturn(new ActorContext(new TenantId(7L), new com.shiyu.ai.kernel.context.UserId(9L), false));
            mocked.when(ActorContextHttpAdapter::userId).thenReturn(9L);
            assertFalse(controller.getAuthCodes().isSuccess());
        }
        doThrow(new IllegalStateException("logout")).when(auth).logout(anyString());
        assertFalse(controller.logout("Bearer token").isSuccess());
    }
}

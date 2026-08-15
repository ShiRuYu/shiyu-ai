package com.shiyu.ai.web.auth;

import com.shiyu.ai.auth.handler.LoginRateLimiter;
import com.shiyu.ai.auth.request.LoginRequest;
import com.shiyu.ai.auth.request.RefreshTokenRequest;
import com.shiyu.ai.auth.service.AuthService;
import com.shiyu.ai.auth.service.UserService;
import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.knowledge.service.KnowledgeSpaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("dev")
class AuthControllerTest {

    @AfterEach
    void clearUserContext() {
        UserContextHolder.clearContext();
    }

    @Test
    void initializesTenantDefaultsWithAuthenticatedAuditContext() {
        AuthService authService = mock(AuthService.class);
        UserService userService = mock(UserService.class);
        LoginRateLimiter rateLimiter = mock(LoginRateLimiter.class);
        KnowledgeSpaceService knowledgeSpaceService = mock(KnowledgeSpaceService.class);
        AuthController controller = new AuthController(
                authService, userService, rateLimiter, knowledgeSpaceService);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");
        LoginResponseVO response = new LoginResponseVO();
        response.setId(2L);
        response.setUsername("admin");
        response.setHomeTenantId(1L);
        response.setCurrentTenantId(1L);
        response.setSwitchMode("NORMAL");
        when(rateLimiter.getClientIp()).thenReturn("127.0.0.1");
        when(rateLimiter.isAllowed("127.0.0.1")).thenReturn(true);
        when(authService.login("admin", "123456", null)).thenReturn(response);

        AtomicReference<UserContext> observedContext = new AtomicReference<>();
        doAnswer(invocation -> {
            observedContext.set(UserContextHolder.getContext());
            return null;
        }).when(knowledgeSpaceService).initializeTenantDefaults(1L);

        Result<LoginResponseVO> result = controller.login(request);

        assertSame(response, result.getData());
        assertEquals(2L, observedContext.get().getUserId());
        assertEquals("admin", observedContext.get().getUsername());
        assertEquals(1L, observedContext.get().getCurrentTenantId());
        assertNull(UserContextHolder.getContext());
        verify(knowledgeSpaceService).initializeTenantDefaults(1L);
        verify(rateLimiter).reset("127.0.0.1");
    }

    @Test
    void rotatesAValidAccessTokenFromTheRequestBody() {
        AuthService authService = mock(AuthService.class);
        UserService userService = mock(UserService.class);
        LoginRateLimiter rateLimiter = mock(LoginRateLimiter.class);
        KnowledgeSpaceService knowledgeSpaceService = mock(KnowledgeSpaceService.class);
        AuthController controller = new AuthController(
                authService, userService, rateLimiter, knowledgeSpaceService);
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setAccessToken("old-access-token");
        when(authService.refreshToken("old-access-token")).thenReturn("new-access-token");

        Result<String> result = controller.refreshToken(request);

        assertEquals(200, result.getCode());
        assertEquals("new-access-token", result.getData());
        verify(authService).refreshToken("old-access-token");
    }
}

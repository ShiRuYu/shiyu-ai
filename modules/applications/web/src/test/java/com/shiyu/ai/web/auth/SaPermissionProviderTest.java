package com.shiyu.ai.web.auth;

import com.shiyu.ai.auth.port.repository.AuthRepository;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Tag("dev")
class SaPermissionProviderTest {

    @AfterEach
    void clearContext() {
        UserContextHolder.clearContext();
    }

    @Test
    void deniesPermissionLookupWhenAuthenticationContextIsMissing() {
        AuthRepository repository = mock(AuthRepository.class);
        SaPermissionProvider provider = new SaPermissionProvider(repository);

        assertTrue(provider.getPermissionList(12L, "login").isEmpty());
        assertTrue(provider.getRoleList(12L, "login").isEmpty());
        verify(repository, never()).selectCodesByUserIdAndRoleCode(eq(new UserId(12L)), any(TenantId.class), eq(""));
        verify(repository, never()).selectRoleCodesByUserId(eq(new UserId(12L)), any(TenantId.class));
    }

    @Test
    void queriesPermissionsWithTheExplicitTenantAndActiveRole() {
        UserContext context = new UserContext();
        context.setUserId(12L);
        context.setCurrentTenantId(11L);
        context.setCurrentRoleCode("member");
        UserContextHolder.setContext(context);

        AuthRepository repository = mock(AuthRepository.class);
        when(repository.selectCodesByUserIdAndRoleCode(new UserId(12L), new TenantId(11L), "member"))
                .thenReturn(List.of("agent:read"));
        when(repository.selectRoleCodesByUserId(new UserId(12L), new TenantId(11L))).thenReturn(List.of("member"));
        SaPermissionProvider provider = new SaPermissionProvider(repository);

        assertEquals(List.of("agent:read"), provider.getPermissionList(12L, "login"));
        assertEquals(List.of("member"), provider.getRoleList(12L, "login"));
        verify(repository).selectCodesByUserIdAndRoleCode(new UserId(12L), new TenantId(11L), "member");
        verify(repository).selectRoleCodesByUserId(new UserId(12L), new TenantId(11L));
    }

    @Test
    void rejectsLoginIdThatDoesNotMatchTheAuthenticatedActor() {
        UserContext context = new UserContext();
        context.setUserId(12L);
        context.setCurrentTenantId(11L);
        context.setCurrentRoleCode("member");
        UserContextHolder.setContext(context);

        AuthRepository repository = mock(AuthRepository.class);
        SaPermissionProvider provider = new SaPermissionProvider(repository);

        assertTrue(provider.getPermissionList(99L, "login").isEmpty());
        assertTrue(provider.getRoleList(99L, "login").isEmpty());
        verifyNoInteractions(repository);
    }
}

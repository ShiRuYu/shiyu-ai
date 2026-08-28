package com.shiyu.ai.auth.web;

import com.shiyu.ai.auth.api.request.AuthCodeRequest;
import com.shiyu.ai.auth.service.AuthCodeService;
import com.shiyu.ai.auth.request.AuthCodePageRequest;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthCodeControllerCoverageTest {
    private final AuthCodeService service = mock(AuthCodeService.class);
    private final AuthCodeController controller = new AuthCodeController(service);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void mapsAllAuthCodeOperationsAndSuccessBranches() {
        AuthCodeRequest request = new AuthCodeRequest();
        AuthCodePageRequest page = new AuthCodePageRequest();
        when(service.list(any())).thenReturn(List.of());
        when(service.listRoleAuthCodes(any(), eq(3L), eq(new TenantId(7L)))).thenReturn(List.of("read"));
        when(service.options(any())).thenReturn(List.of());
        when(service.update(any(), eq(1L), same(request))).thenReturn(true);
        when(service.delete(any(), eq(1L))).thenReturn(true);
        when(service.grant(any(), eq(3L), eq(new TenantId(7L)), eq(List.of(5L)))).thenReturn(true);
        when(service.replace(any(), eq(3L), eq(new TenantId(7L)), eq(List.of("read")))).thenReturn(true);
        when(service.revoke(any(), eq(3L), eq(new TenantId(7L)), eq(5L))).thenReturn(true);
        assertTrue(controller.list().isSuccess());
        assertEquals(List.of("read"), controller.listRoleAuthCodes(3L, 7L).getData());
        assertTrue(controller.options().isSuccess());
        assertTrue(controller.create(request).isSuccess());
        assertTrue(controller.update(1L, request).isSuccess());
        assertTrue(controller.delete(1L).isSuccess());
        assertTrue(controller.grant(3L, 7L, List.of(5L)).isSuccess());
        assertTrue(controller.replace(3L, 7L, List.of("read")).isSuccess());
        assertTrue(controller.revoke(3L, 7L, 5L).isSuccess());
        assertTrue(controller.page(page).isSuccess());
    }

    @Test
    void mapsFailedMutations() {
        AuthCodeRequest request = new AuthCodeRequest();
        when(service.update(any(), anyLong(), any())).thenReturn(false);
        when(service.delete(any(), anyLong())).thenReturn(false);
        when(service.grant(any(), anyLong(), any(TenantId.class), anyList())).thenReturn(false);
        when(service.replace(any(), anyLong(), any(TenantId.class), anyList())).thenReturn(false);
        when(service.revoke(any(), anyLong(), any(TenantId.class), anyLong())).thenReturn(false);
        assertFalse(controller.update(1L, request).isSuccess());
        assertFalse(controller.delete(1L).isSuccess());
        assertFalse(controller.grant(1L, 7L, List.of()).isSuccess());
        assertFalse(controller.replace(1L, 7L, List.of()).isSuccess());
        assertFalse(controller.revoke(1L, 7L, 1L).isSuccess());
    }
}

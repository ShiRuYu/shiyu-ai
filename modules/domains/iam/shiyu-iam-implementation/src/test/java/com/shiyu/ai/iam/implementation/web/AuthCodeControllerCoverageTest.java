package com.shiyu.ai.iam.implementation.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.common.foundation.context.model.UserContext;
import com.shiyu.ai.common.foundation.context.UserContextHolder;
import com.shiyu.ai.iam.implementation.api.request.AuthCodeRequest;
import com.shiyu.ai.iam.implementation.request.AuthCodePageRequest;
import com.shiyu.ai.iam.implementation.service.AuthCodeService;
import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 验证 认证 Code Controller Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class AuthCodeControllerCoverageTest {
    private final AuthCodeService service = mock(AuthCodeService.class);
    private final AuthCodeController controller = new AuthCodeController(service);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L);
        context.setCurrentTenantId(7L);
        context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() {
        UserContextHolder.clearContext();
    }

    @Test
    void mapsAllAuthCodeOperationsAndSuccessBranches() {
        AuthCodeRequest request = new AuthCodeRequest();
        AuthCodePageRequest page = new AuthCodePageRequest();
        when(service.listRoleAuthCodes(any(), eq(3L), eq(new TenantId(7L))))
                .thenReturn(List.of("read"));
        when(service.options(any())).thenReturn(List.of());
        when(service.update(any(), eq(1L), same(request))).thenReturn(true);
        when(service.delete(any(), eq(1L))).thenReturn(true);
        when(service.grant(any(), eq(3L), eq(new TenantId(7L)), eq(List.of(5L)))).thenReturn(true);
        when(service.replace(any(), eq(3L), eq(new TenantId(7L)), eq(List.of("read"))))
                .thenReturn(true);
        when(service.revoke(any(), eq(3L), eq(new TenantId(7L)), eq(5L))).thenReturn(true);
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

    @Test
    void keepsOptionsAsTheSingleUnpagedCollectionEndpoint() throws NoSuchMethodException {
        GetMapping optionsMapping =
                AuthCodeController.class.getMethod("options").getAnnotation(GetMapping.class);

        assertNotNull(optionsMapping);
        assertArrayEquals(new String[] {"/options"}, optionsMapping.value());
    }
}

package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.request.TenantRequest;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class TenantServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(10), new UserId(4), false);
    private final TenantRepository repository = mock(TenantRepository.class);
    private final TenantServiceImpl service = new TenantServiceImpl(repository);

    @Test
    void scopesViewsAndPageToActorTenant() {
        TenantBO tenant = new TenantBO(); tenant.setId(11L); tenant.setParentId(10L); tenant.setName("child");
        TenantVO view = mock(TenantVO.class);
        when(repository.selectAll()).thenReturn(List.of(tenant)); when(repository.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(11L));
        when(repository.selectPage(ACTOR.tenantId(), 1, 10, "", "", 1)).thenReturn(Pair.of(1L, List.of(tenant)));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TenantVO.class))).thenReturn(List.of(view));
            assertEquals(1, service.allTenantsView(ACTOR).size());
            assertEquals(1, service.getTenantPage(ACTOR, 1, 10, "", "", 1).getItems().size());
        }
        verify(repository).selectPage(ACTOR.tenantId(), 1, 10, "", "", 1);
    }

    @Test
    void enforcesTenantCreationUpdateAndDeletionRules() {
        TenantRequest request = new TenantRequest(); request.setCode("child"); request.setName("Child");
        TenantBO converted = new TenantBO(); converted.setCode("child"); converted.setName("Child");
        when(repository.existsByCode("child", null)).thenReturn(false); when(repository.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L, 11L));
        when(repository.insert(any(TenantBO.class), eq(ACTOR.tenantId()))).thenReturn(converted);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, TenantBO.class)).thenReturn(converted);
            assertTrue(service.createTenant(ACTOR, request));
        }
        when(repository.selectById(11L)).thenReturn(converted); when(repository.update(any(TenantBO.class))).thenReturn(true);
        assertFalse(service.deleteTenant(ACTOR, 10L));
        assertTrue(service.deleteTenant(ACTOR, 11L));
        verify(repository).cascadeDelete(new TenantId(11L));
        assertThrows(IllegalArgumentException.class, () -> service.getTenantPage(null, 1, 10, "", "", 1));
    }

    @Test
    void rejectsInvalidTargetTenantBeforeRepositoryAccess() {
        assertNull(service.detailView(ACTOR, 0L));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(TenantRequest.class), eq(TenantBO.class)))
                    .thenReturn(new TenantBO());
            assertFalse(service.updateTenant(ACTOR, 0L, new TenantRequest()));
        }
        assertFalse(service.deleteTenant(ACTOR, 0L));

        TenantRequest childRequest = new TenantRequest();
        childRequest.setCode("child");
        TenantBO child = new TenantBO();
        child.setCode("child");
        child.setParentId(0L);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(childRequest, TenantBO.class)).thenReturn(child);
            when(repository.existsByCode("child", null)).thenReturn(false);
            assertFalse(service.createTenant(ACTOR, childRequest));
        }

        verify(repository, never()).selectById(0L);
        verify(repository, never()).update(any(TenantBO.class));
        verify(repository, never()).cascadeDelete(any(TenantId.class));
        verify(repository, never()).selectDescendantIds(argThat(id -> id.value() == 0L));
    }
}

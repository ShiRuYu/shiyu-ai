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
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
class TenantServiceDeepCoverageTest {
    @Test
    void coversPlatformAdminViewsAndInaccessibleDetails() {
        TenantRepository repository = mock(TenantRepository.class);
        TenantServiceImpl service = new TenantServiceImpl(repository);
        TenantBO root = new TenantBO(); root.setId(10L); root.setParentId(null);
        TenantBO child = new TenantBO(); child.setId(11L); child.setParentId(10L);
        when(repository.selectAll()).thenReturn(List.of(root, child));
        ActorContext platformAdmin = new ActorContext(new TenantId(10L), new UserId(4L), true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TenantVO.class))).thenReturn(List.of());
            assertTrue(service.allTenantsView(platformAdmin).isEmpty());
            mapper.when(() -> MapstructUtils.convert((TenantBO) null, TenantVO.class)).thenReturn(null);
            assertFalse(service.detailView(new ActorContext(new TenantId(10L), new UserId(4L), false), 99L) != null);
        }
        when(repository.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L, 11L));
        when(repository.selectPage(new TenantId(10L), 1, 10, null, null, null)).thenReturn(Pair.of(0L, List.of()));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(any(List.class), eq(TenantVO.class))).thenReturn(List.of());
            assertTrue(service.getTenantPage(platformAdmin, 1, 10, null, null, null).getItems().isEmpty());
        }

        try {
            Method treeMethod = TenantServiceImpl.class.getDeclaredMethod("getTenantTree", ActorContext.class);
            treeMethod.setAccessible(true);
            List<TenantBO> tree = (List<TenantBO>) treeMethod.invoke(service, platformAdmin);
            assertEquals(1, tree.size());
            assertEquals(1, tree.get(0).getChildren().size());
            when(repository.selectAll()).thenReturn(List.of());
            assertTrue(((List<?>) treeMethod.invoke(service, platformAdmin)).isEmpty());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    void coversCreateUpdateAndDeleteScopeFailures() {
        TenantRepository repository = mock(TenantRepository.class);
        TenantServiceImpl service = new TenantServiceImpl(repository);
        ActorContext actor = new ActorContext(new TenantId(10L), new UserId(4L), false);
        TenantRequest request = new TenantRequest(); request.setCode("child"); request.setName("Child");
        TenantBO converted = new TenantBO(); converted.setCode("child"); converted.setName("Child");
        when(repository.existsByCode("child", null)).thenReturn(true);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, TenantBO.class)).thenReturn(converted);
            assertFalse(service.createTenant(actor, request));
        }
        when(repository.existsByCode("child", null)).thenReturn(false);
        converted.setParentId(99L);
        when(repository.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, TenantBO.class)).thenReturn(converted);
            assertFalse(service.createTenant(actor, request));
        }

        when(repository.selectById(20L)).thenReturn(null);
        assertFalse(update(service, repository, actor, 20L, new TenantBO()));
        TenantBO existing = new TenantBO(); existing.setId(21L); existing.setCode("old"); existing.setParentId(10L);
        when(repository.selectById(21L)).thenReturn(existing);
        when(repository.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L, 21L));
        TenantBO outsideParent = new TenantBO(); outsideParent.setParentId(99L);
        assertFalse(update(service, repository, actor, 21L, outsideParent));
        TenantBO cycle = new TenantBO(); cycle.setParentId(22L);
        when(repository.selectDescendantIds(new TenantId(21L))).thenReturn(List.of(22L));
        when(repository.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L, 21L, 22L));
        assertFalse(update(service, repository, actor, 21L, cycle));
        TenantBO duplicate = new TenantBO(); duplicate.setCode("new");
        when(repository.existsByCode("new", 21L)).thenReturn(true);
        assertFalse(update(service, repository, actor, 21L, duplicate));
        TenantBO successful = new TenantBO(); successful.setCode("old");
        when(repository.update(any(TenantBO.class))).thenReturn(true);
        assertTrue(update(service, repository, actor, 21L, successful));

        assertFalse(service.deleteTenant(actor, 1L));
        assertFalse(service.deleteTenant(actor, 10L));
        when(repository.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L));
        assertFalse(service.deleteTenant(actor, 99L));
        assertThrows(IllegalArgumentException.class, () -> service.deleteTenant(null, 99L));
    }

    private boolean update(TenantServiceImpl service, TenantRepository repository, ActorContext actor,
                           long id, TenantBO value) {
        TenantRequest request = new TenantRequest();
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, TenantBO.class)).thenReturn(value);
            return service.updateTenant(actor, id, request);
        }
    }
}

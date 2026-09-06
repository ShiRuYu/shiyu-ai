package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.auth.port.repository.MenuRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.request.MenuRequest;
import com.shiyu.ai.auth.vo.MenuVO;
import com.shiyu.ai.auth.vo.RouteMenuVO;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MenuServiceImplTest {
    private final MenuRepository menus = mock(MenuRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private MenuServiceImpl service;
    private ActorContext actor;

    @BeforeEach
    void setUp() {
        service = new MenuServiceImpl(menus, tenants);
        actor = new ActorContext(new TenantId(7), new UserId(8), false);
    }

    @Test
    void buildsRouteTreesAndUsesTenantScopedCache() {
        MenuBO root = menu(1L, null, "CATALOG", 7L);
        root.setName("Root"); root.setCode("root"); root.setShow(true);
        MenuBO child = menu(2L, 1L, "MENU", 7L);
        child.setName("Child"); child.setCode("child"); child.setShow(false); child.setLayout("none");
        when(menus.selectMenusByUserId(new TenantId(7), 8L, null, false, null)).thenReturn(List.of(root, child));

        List<RouteMenuVO> first = service.routeMenusView(actor);
        List<RouteMenuVO> cached = service.routeMenusView(actor);
        assertEquals(1, first.size());
        assertEquals(1, first.getFirst().getChildren().size());
        assertEquals("catalog", first.getFirst().getType());
        assertEquals("menu", first.getFirst().getChildren().getFirst().getType());
        assertEquals(first, cached);
        verify(menus, times(1)).selectMenusByUserId(new TenantId(7), 8L, null, false, null);
        service.evictRouteMenuCache(8L);
        service.evictAllRouteMenuCache();
    }

    @Test
    void exposesQueriesCreatesUpdatesDeletesAndPage() {
        MenuBO root = menu(1L, null, "MENU", 7L);
        MenuBO child = menu(2L, 1L, "LINK", 7L);
        when(menus.selectAll(new TenantId(7))).thenReturn(List.of(root, child));
        when(menus.selectAllByType(new TenantId(7), "MENU")).thenReturn(List.of(root));
        when(menus.selectByParentId(new TenantId(7), 1L)).thenReturn(List.of(child));
        when(menus.selectMenusByUserId(new TenantId(7), 8L, null, false, null)).thenReturn(List.of(root));
        when(menus.existsByName(new TenantId(7), "root", null)).thenReturn(true);
        when(menus.existsByPath(new TenantId(7), "/root", null)).thenReturn(true);
        when(menus.selectById(new TenantId(7), 1L)).thenReturn(root);
        when(menus.deleteById(new TenantId(7), 1L)).thenReturn(true);
        when(menus.update(any(MenuBO.class))).thenReturn(true);
        when(menus.insert(any(MenuBO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(1, service.treeView(actor).getFirst().getChildren().size());
        assertEquals(1, service.menuRootsView(actor).size());
        assertEquals(1, service.childrenView(actor, 1L).size());
        assertEquals(1, service.permissionsView(actor).size());
        assertTrue(service.isMenuNameExists(actor, "root", null));
        assertTrue(service.isMenuPathExists(actor, "/root", null));
        assertTrue(service.deleteMenu(actor, 1L));

        MenuRequest request = new MenuRequest();
        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(any(MenuRequest.class), eq(MenuBO.class))).thenReturn(menu(3L, null, "menu", 0L));
            assertTrue(service.createMenu(actor, request));
            assertTrue(service.updateMenu(actor, 1L, request));
        }

        Pair<Long, List<MenuBO>> page = Pair.of(1L, List.of(root));
        when(menus.selectPage(new TenantId(7), 1, 10, null, null, null, null)).thenReturn(page);
        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(anyList(), eq(MenuVO.class))).thenReturn(List.of(new MenuVO()));
            assertEquals(1L, service.getMenuPage(actor, 1, 10, null, null, null, null).getTotal());
        }
    }

    @Test
    void rejectsMissingActorAndUnsupportedMenuType() {
        assertThrows(IllegalArgumentException.class, () -> service.treeView(null));
        MenuRequest request = new MenuRequest();
        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(any(MenuRequest.class), eq(MenuBO.class))).thenReturn(menu(3L, null, "BUTTON", 7L));
            assertFalse(service.createMenu(actor, request));
        }
    }

    @Test
    void rejectsMenusWhoseParentIsMissingOrOwnedByAnotherTenant() {
        MenuRequest request = new MenuRequest();
        MenuBO child = menu(3L, 99L, "MENU", 7L);
        when(menus.selectById(new TenantId(7), 99L)).thenReturn(null);
        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(any(MenuRequest.class), eq(MenuBO.class))).thenReturn(child);
            assertFalse(service.createMenu(actor, request));
            assertFalse(service.updateMenu(actor, 3L, request));
        }

        MenuBO foreignParent = menu(100L, null, "MENU", 8L);
        child.setParentId(100L);
        when(menus.selectById(new TenantId(7), 100L)).thenReturn(foreignParent);
        try (MockedStatic<MapstructUtils> mapstruct = mockStatic(MapstructUtils.class)) {
            mapstruct.when(() -> MapstructUtils.convert(any(MenuRequest.class), eq(MenuBO.class))).thenReturn(child);
            assertFalse(service.createMenu(actor, request));
        }
    }

    private static MenuBO menu(Long id, Long parentId, String type, Long tenantId) {
        MenuBO menu = new MenuBO(); menu.setId(id); menu.setParentId(parentId); menu.setType(type); menu.setTenantId(tenantId); menu.setStatus(1); menu.setOrder(id == null ? 0 : id.intValue()); return menu;
    }
}

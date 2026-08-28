package com.shiyu.ai.auth.web;

import com.shiyu.ai.auth.request.MenuPageRequest;
import com.shiyu.ai.auth.request.MenuRequest;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.auth.vo.MenuVO;
import com.shiyu.ai.auth.vo.RouteMenuVO;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MenuControllerCoverageTest {
    private final MenuService service = mock(MenuService.class);
    private final MenuController controller = new MenuController(service);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L);
        context.setCurrentTenantId(7L);
        context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
        when(service.routeMenusView(any())).thenReturn(List.of());
        when(service.allTreeView(any())).thenReturn(List.of());
        when(service.menuRootsView(any())).thenReturn(List.of());
        when(service.childrenView(any(), anyLong())).thenReturn(List.of());
        when(service.permissionsView(any())).thenReturn(List.of());
        when(service.treeView(any())).thenReturn(List.of());
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void mapsAllMenuQueriesMutationsAndExistenceChecks() {
        assertTrue(controller.getAllMenus().isSuccess());
        assertTrue(controller.getSystemMenuList().isSuccess());
        MenuPageRequest page = new MenuPageRequest();
        page.setPageNum(2); page.setPageSize(10); page.setName("menu"); page.setCode("menu:list"); page.setType("M"); page.setStatus(1);
        assertTrue(controller.getMenuPage(page).isSuccess());
        assertTrue(controller.getMenuRoots().isSuccess());
        assertTrue(controller.getMenuChildren(3L).isSuccess());
        assertTrue(controller.getMenuPermissionsTree().isSuccess());
        assertTrue(controller.getAllTree().isSuccess());

        MenuRequest request = new MenuRequest();
        when(service.deleteMenu(any(), eq(3L))).thenReturn(true);
        when(service.createMenu(any(), same(request))).thenReturn(true);
        when(service.updateMenu(any(), eq(3L), same(request))).thenReturn(true);
        when(service.isMenuNameExists(any(), eq("Home"), isNull())).thenReturn(true);
        when(service.isMenuPathExists(any(), eq("/home"), eq(3L))).thenReturn(false);
        assertTrue(controller.deleteMenu(3L).isSuccess());
        assertTrue(controller.createMenu(request).isSuccess());
        assertTrue(controller.updateMenu(3L, request).isSuccess());
        assertTrue(controller.isMenuNameExists("Home", null).getData());
        assertFalse(controller.isMenuPathExists("/home", 3L).getData());
    }

    @Test
    void mapsMutationFailuresAndMenuServiceFailure() {
        when(service.routeMenusView(any())).thenThrow(new IllegalStateException("db"));
        assertFalse(controller.getAllMenus().isSuccess());
        when(service.deleteMenu(any(), anyLong())).thenReturn(false);
        when(service.createMenu(any(), any())).thenReturn(false);
        when(service.updateMenu(any(), anyLong(), any())).thenReturn(false);
        assertFalse(controller.deleteMenu(1L).isSuccess());
        assertFalse(controller.createMenu(new MenuRequest()).isSuccess());
        assertFalse(controller.updateMenu(1L, new MenuRequest()).isSuccess());
    }
}

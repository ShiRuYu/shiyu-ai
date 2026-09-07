package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.model.MenuBO;
import com.shiyu.ai.auth.persistence.dataobject.MenuDO;
import com.shiyu.ai.auth.persistence.mapper.MenuMapper;
import com.shiyu.ai.auth.persistence.mapper.RoleScopeMenuMapper;
import com.shiyu.ai.auth.persistence.mapper.TenantMenuMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class MenuRepositorySecurityTest {
    private static final TenantId TENANT = new TenantId(21);

    @Test
    void coversTenantScopedMenuQueriesCrudAndSubtreeDeletion() throws Exception {
        MenuMapper menus = mock(MenuMapper.class); RoleScopeMenuMapper roleMenus = mock(RoleScopeMenuMapper.class); TenantMenuMapper tenantMenus = mock(TenantMenuMapper.class);
        MenuRepositoryImpl repository = new MenuRepositoryImpl(); inject(repository, "menuMapper", menus); inject(repository, "roleScopeMenuMapper", roleMenus); inject(repository, "tenantMenuMapper", tenantMenus);
        MenuDO root = menu(1L, null); MenuDO child = menu(2L, 1L);
        when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(root, child));
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(MenuBO.class))).thenReturn(List.of(new MenuBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(MenuBO.class))).thenReturn(new MenuBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(MenuDO.class))).thenReturn(new MenuDO());
            assertEquals(1, repository.selectAll(TENANT).size());
            assertEquals(1, repository.selectAllByType(TENANT, "MENU").size());
            assertEquals(1, repository.selectAllExcludingType(TENANT, "BUTTON").size());
            when(menus.selectOneByQuery(any(QueryWrapper.class))).thenReturn(root);
            assertNotNull(repository.selectById(TENANT, 1L));
            MenuBO menu = new MenuBO(); menu.setTenantId(21L);
            when(menus.insertSelective(any(MenuDO.class))).thenAnswer(i -> { ((MenuDO) i.getArgument(0)).setId(9L); return 1; });
            assertEquals(9L, repository.insert(menu).getId()); repository.update(menu);
            when(menus.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L, 0L, 1L, 0L);
            assertTrue(repository.existsByName(TENANT, "Home", null)); assertFalse(repository.existsByPath(TENANT, "/home", 9L));
            // Exercise every optional management filter and explicit pagination
            // branch; the security predicate must still be added to both count
            // and page queries.
            when(menus.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L, 0L);
            when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(root));
            assertEquals(1L, repository.selectPage(TENANT, 2, 5,
                    "Home", "home", "menu", 1).getLeft());
            when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(child), List.of());
            when(menus.selectOneByQuery(any(QueryWrapper.class))).thenReturn(root, child);
            when(menus.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
            assertTrue(repository.deleteById(TENANT, 1L));
            assertEquals(0L, repository.selectPage(TENANT, null, null, "", "", "", null).getLeft());
            assertEquals(1, repository.selectByParentId(TENANT, null).size()); assertEquals(1, repository.selectByParentIdAndType(TENANT, null, "MENU").size());
            assertEquals(1, repository.selectMenusByUserId(TENANT, 8L, "member", false, "BUTTON").size());
            assertEquals(1, repository.selectMenusByUserId(TENANT, 8L, "member", true, null).size());
        }
        when(menus.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        assertFalse(repository.deleteById(TENANT, 99L));
        assertThrows(IllegalArgumentException.class, () -> repository.selectAll(null));
        assertThrows(IllegalArgumentException.class, () -> repository.selectMenusByUserId(null, 8L, null, false, null));
    }

    private static MenuDO menu(Long id, Long parent) { MenuDO value = new MenuDO(); value.setId(id); value.setParentId(parent); value.setDelFlag(0); return value; }
    private static void inject(Object target, String name, Object value) throws Exception { Field f = target.getClass().getDeclaredField(name); f.setAccessible(true); f.set(target, value); }
}

package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.persistence.dataobject.TenantDO;
import com.shiyu.ai.auth.persistence.dataobject.UserDO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.MenuDO;
import com.shiyu.ai.auth.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.persistence.dataobject.TenantAuthCodeDO;
import com.shiyu.ai.auth.persistence.mapper.TenantMapper;
import com.shiyu.ai.auth.persistence.mapper.*;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class TenantRepositorySecurityTest {
    @Test
    void scopesTenantTreeQueriesAndRejectsMissingTenant() throws Exception {
        TenantMapper mapper = mock(TenantMapper.class);
        TenantDO root = tenant(1L, null); TenantDO child = tenant(2L, 1L); TenantDO grandchild = tenant(3L, 2L);
        when(mapper.selectAll()).thenReturn(List.of(root, child, grandchild));
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(3L);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(root, child));
        TenantRepositoryImpl repository = new TenantRepositoryImpl(); inject(repository, "tenantMapper", mapper);
        assertEquals(List.of(1L, 2L, 3L).size(), repository.selectDescendantIds(new TenantId(1L)).size());
        assertEquals(1L, repository.selectRootTenantId(new TenantId(3L)));
        assertThrows(IllegalArgumentException.class, () -> repository.selectRootTenantId(null));
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(TenantBO.class))).thenReturn(List.of(new TenantBO()));
            Pair<Long, List<TenantBO>> page = repository.selectPage(new TenantId(1L), null, null, "root", "root", 1);
            assertEquals(3L, page.getLeft()); assertEquals(1, page.getRight().size());
            Pair<Long, List<TenantBO>> unfiltered = repository.selectPage(new TenantId(1L), 2, 20, " ", " ", null);
            assertEquals(3L, unfiltered.getLeft());
            assertTrue(repository.existsByCode("root", null));
            when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
            assertFalse(repository.existsByCode("root", 1L));
            TenantBO value = new TenantBO();
            TenantDO converted = new TenantDO();
            conversions.when(() -> MapstructUtils.convert(value, TenantDO.class)).thenReturn(converted);
            when(mapper.update(any(TenantDO.class))).thenReturn(1);
            when(mapper.deleteById(1L)).thenReturn(1);
            assertTrue(repository.update(value)); assertTrue(repository.deleteById(1L));
        }
        assertThrows(IllegalArgumentException.class, () -> repository.selectPage(null, 1, 10, null, null, null));
    }

    @Test
    void loadsAndCreatesTenantSecurityAndCascadesWithoutCrossTenantQueries() throws Exception {
        TenantMapper tenants = mock(TenantMapper.class);
        UserMapper users = mock(UserMapper.class);
        RoleMapper roles = mock(RoleMapper.class);
        MenuMapper menus = mock(MenuMapper.class);
        AuthCodeMapper authCodes = mock(AuthCodeMapper.class);
        UserScopeRoleMapper userRoles = mock(UserScopeRoleMapper.class);
        RoleScopeMenuMapper roleMenus = mock(RoleScopeMenuMapper.class);
        RoleScopeAuthCodeMapper roleCodes = mock(RoleScopeAuthCodeMapper.class);
        TenantMenuMapper tenantMenus = mock(TenantMenuMapper.class);
        TenantAuthCodeMapper tenantCodes = mock(TenantAuthCodeMapper.class);
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        inject(repository, "tenantMapper", tenants); inject(repository, "userMapper", users);
        inject(repository, "roleMapper", roles); inject(repository, "menuMapper", menus);
        inject(repository, "authCodeMapper", authCodes); inject(repository, "userScopeRoleMapper", userRoles);
        inject(repository, "roleScopeMenuMapper", roleMenus); inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        inject(repository, "tenantMenuMapper", tenantMenus); inject(repository, "tenantAuthCodeMapper", tenantCodes);
        TenantDO root = tenant(11L, null);
        when(tenants.selectAll()).thenReturn(List.of(root));
        when(tenants.selectOneById(11L)).thenReturn(root);
        when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        when(userRoles.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        when(users.insertSelective(any(UserDO.class))).thenAnswer(invocation -> {
            UserDO user = invocation.getArgument(0); user.setId(99L); return 1;
        });
        when(roles.insertSelective(any(RoleDO.class))).thenAnswer(invocation -> {
            RoleDO role = invocation.getArgument(0); role.setId(88L); return 1;
        });
        TenantBO convertedValue = new TenantBO();
        TenantDO converted = tenant(12L, 11L);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(TenantBO.class))).thenReturn(List.of(convertedValue));
            conversions.when(() -> MapstructUtils.convert(root, TenantBO.class)).thenReturn(convertedValue);
            conversions.when(() -> MapstructUtils.convert(any(TenantBO.class), eq(TenantDO.class))).thenReturn(converted);
            assertEquals(1, repository.selectAll().size());
            assertEquals(convertedValue, repository.selectById(11L));
            TenantBO input = new TenantBO(); input.setCode("child"); input.setName("Child");
            assertEquals(12L, repository.insert(input, new TenantId(11L)).getId());
            repository.cascadeDelete(new TenantId(11L));
            verify(roles).insertSelective(any(RoleDO.class));
            verify(users).insertSelective(any(UserDO.class));
            verify(tenants).deleteByQuery(any(QueryWrapper.class));
        }
    }

    @Test
    void clonesSelectedMenusWithAncestorsInDepthOrder() throws Exception {
        MenuMapper menus = mock(MenuMapper.class);
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        inject(repository, "menuMapper", menus);
        MenuDO root = menu(1L, null, "root");
        MenuDO group = menu(2L, 1L, "group");
        MenuDO leaf = menu(3L, 2L, "leaf");
        when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(root, group, leaf));
        when(menus.insertSelective(any(MenuDO.class))).thenAnswer(invocation -> {
            MenuDO target = invocation.getArgument(0);
            target.setId(100L + target.getName().length());
            return 1;
        });
        Method clone = TenantRepositoryImpl.class.getDeclaredMethod(
                "cloneMenusForTenant", Long.class, Long.class, List.class);
        clone.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Long> cloned = (List<Long>) clone.invoke(repository, 10L, 20L, Arrays.asList(3L, 999L, null));
        assertEquals(3, cloned.size());
        verify(menus, times(3)).insertSelective(any(MenuDO.class));
        @SuppressWarnings("unchecked")
        List<Long> empty = (List<Long>) clone.invoke(repository, 20L, 21L, List.of());
        assertTrue(empty.isEmpty());
    }

    @Test
    void initializesTenantSecurityWithCopiedMenusAndValidatedAuthCodes() throws Exception {
        TenantMapper tenants = mock(TenantMapper.class);
        UserMapper users = mock(UserMapper.class);
        RoleMapper roles = mock(RoleMapper.class);
        MenuMapper menus = mock(MenuMapper.class);
        AuthCodeMapper authCodes = mock(AuthCodeMapper.class);
        UserScopeRoleMapper userRoles = mock(UserScopeRoleMapper.class);
        RoleScopeMenuMapper roleMenus = mock(RoleScopeMenuMapper.class);
        RoleScopeAuthCodeMapper roleCodes = mock(RoleScopeAuthCodeMapper.class);
        TenantMenuMapper tenantMenus = mock(TenantMenuMapper.class);
        TenantAuthCodeMapper tenantCodes = mock(TenantAuthCodeMapper.class);
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        inject(repository, "tenantMapper", tenants); inject(repository, "userMapper", users);
        inject(repository, "roleMapper", roles); inject(repository, "menuMapper", menus);
        inject(repository, "authCodeMapper", authCodes); inject(repository, "userScopeRoleMapper", userRoles);
        inject(repository, "roleScopeMenuMapper", roleMenus); inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        inject(repository, "tenantMenuMapper", tenantMenus); inject(repository, "tenantAuthCodeMapper", tenantCodes);

        MenuDO root = menu(1L, null, "root");
        MenuDO leaf = menu(2L, 1L, "leaf");
        when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(root, leaf));
        when(menus.insertSelective(any(MenuDO.class))).thenAnswer(invocation -> {
            MenuDO value = invocation.getArgument(0);
            value.setId(value.getName().equals("root") ? 101L : 102L);
            return 1;
        });
        com.shiyu.ai.auth.persistence.dataobject.TenantAuthCodeDO relation =
                new com.shiyu.ai.auth.persistence.dataobject.TenantAuthCodeDO();
        relation.setAuthCodeId(9L);
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(relation));
        com.shiyu.ai.auth.persistence.dataobject.AuthCodeDO code =
                new com.shiyu.ai.auth.persistence.dataobject.AuthCodeDO();
        code.setId(9L); code.setStatus(1); code.setDelFlag(0);
        when(authCodes.selectOneById(9L)).thenReturn(code);
        when(tenants.insertSelective(any(TenantDO.class))).thenAnswer(invocation -> {
            TenantDO value = invocation.getArgument(0); value.setId(20L); return 1;
        });
        when(roles.insertSelective(any(RoleDO.class))).thenAnswer(invocation -> {
            RoleDO value = invocation.getArgument(0); value.setId(30L); return 1;
        });
        when(users.insertSelective(any(UserDO.class))).thenAnswer(invocation -> {
            UserDO value = invocation.getArgument(0); value.setId(40L); return 1;
        });

        TenantBO input = new TenantBO();
        input.setCode("child"); input.setName("Child"); input.setMenuIds(List.of(2L));
        input.setAuthCodeIds(List.of(9L)); input.setAdminRoleName("Child Admin");
        input.setAdminUsername("child-admin"); input.setAdminPassword("secret");
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            TenantDO converted = tenant(20L, 10L);
            conversions.when(() -> MapstructUtils.convert(input, TenantDO.class)).thenReturn(converted);
            TenantBO result = repository.insert(input, new TenantId(10L));
            assertEquals(20L, result.getId());
            verify(tenantMenus).insertBatch(anyList());
            verify(roleMenus).insertBatch(anyList());
            verify(tenantCodes).insertBatch(anyList());
            verify(roleCodes).insertBatch(anyList());
            verify(userRoles).insert(any(UserScopeRoleDO.class));
        }
    }

    @Test
    void cascadeDeletesUnreferencedUsersAndAuthCodesButKeepsSharedRows() throws Exception {
        TenantMapper tenants = mock(TenantMapper.class);
        UserMapper users = mock(UserMapper.class);
        RoleMapper roles = mock(RoleMapper.class);
        MenuMapper menus = mock(MenuMapper.class);
        AuthCodeMapper authCodes = mock(AuthCodeMapper.class);
        UserScopeRoleMapper userRoles = mock(UserScopeRoleMapper.class);
        RoleScopeMenuMapper roleMenus = mock(RoleScopeMenuMapper.class);
        RoleScopeAuthCodeMapper roleCodes = mock(RoleScopeAuthCodeMapper.class);
        TenantMenuMapper tenantMenus = mock(TenantMenuMapper.class);
        TenantAuthCodeMapper tenantCodes = mock(TenantAuthCodeMapper.class);
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        inject(repository, "tenantMapper", tenants); inject(repository, "userMapper", users);
        inject(repository, "roleMapper", roles); inject(repository, "menuMapper", menus);
        inject(repository, "authCodeMapper", authCodes); inject(repository, "userScopeRoleMapper", userRoles);
        inject(repository, "roleScopeMenuMapper", roleMenus); inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        inject(repository, "tenantMenuMapper", tenantMenus); inject(repository, "tenantAuthCodeMapper", tenantCodes);
        TenantDO root = tenant(10L, null);
        when(tenants.selectAll()).thenReturn(List.of(root));
        UserScopeRoleDO userRelation = new UserScopeRoleDO(); userRelation.setUserId(50L);
        TenantAuthCodeDO codeRelation = new TenantAuthCodeDO(); codeRelation.setAuthCodeId(60L);
        when(userRoles.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(userRelation));
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(codeRelation));
        when(userRoles.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
        when(tenantCodes.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);

        repository.cascadeDelete(new TenantId(10L));

        verify(users).deleteById(50L);
        verify(authCodes).deleteById(60L);
        verify(tenants).deleteByQuery(any(QueryWrapper.class));
    }

    private static TenantDO tenant(Long id, Long parentId) {
        TenantDO value = new TenantDO(); value.setId(id); value.setParentId(parentId); return value;
    }

    private static MenuDO menu(Long id, Long parentId, String name) {
        MenuDO value = new MenuDO();
        value.setId(id); value.setParentId(parentId); value.setName(name);
        value.setCode(name); value.setType("MENU"); value.setStatus(1); value.setDelFlag(0);
        return value;
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName); field.setAccessible(true); field.set(target, value);
    }
}

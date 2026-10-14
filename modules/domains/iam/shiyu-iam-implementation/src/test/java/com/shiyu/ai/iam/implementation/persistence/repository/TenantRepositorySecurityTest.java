package com.shiyu.ai.iam.implementation.persistence.repository;

import com.shiyu.ai.iam.implementation.persistence.dataobject.AuthCodeDO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.MenuDO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.RoleDO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantAuthCodeDO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantDO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserDO;
import com.shiyu.ai.iam.implementation.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.*;
import com.shiyu.ai.iam.implementation.persistence.mapper.TenantMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 验证 租户 Repository 安全 相关功能、边界条件、异常路径和协作行为。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class TenantRepositorySecurityTest {
    @Test
    void rootLookupRejectsBrokenOrCyclicParentChains() throws Exception {
        TenantMapper mapper = mock(TenantMapper.class);
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        inject(repository, "tenantMapper", mapper);
        when(mapper.selectAll()).thenReturn(List.of(
                tenant(1L, 2L), tenant(2L, 1L), tenant(3L, 99L)));

        assertNull(repository.selectRootTenantId(new TenantId(1L)));
        assertNull(repository.selectRootTenantId(new TenantId(3L)));
        assertNull(repository.selectRootTenantId(new TenantId(88L)));
    }

    @Test
    void descendantLookupStopsAtCyclesAndExcludesSiblingTrees() throws Exception {
        TenantMapper mapper = mock(TenantMapper.class);
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        inject(repository, "tenantMapper", mapper);
        when(mapper.selectAll()).thenReturn(List.of(
                tenant(1L, 3L), tenant(2L, 1L), tenant(3L, 2L),
                tenant(4L, null), tenant(5L, 4L)));

        assertEquals(java.util.Set.of(1L, 2L, 3L),
                new java.util.HashSet<>(repository.selectDescendantIds(new TenantId(1L))));
        assertEquals(java.util.Set.of(4L, 5L),
                new java.util.HashSet<>(repository.selectDescendantIds(new TenantId(4L))));
    }

    @Test
    void tenantTemplateCopiesOnlyLiveNonPlatformPermissionCodes() throws Exception {
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        TenantAuthCodeMapper links = mock(TenantAuthCodeMapper.class);
        AuthCodeMapper codes = mock(AuthCodeMapper.class);
        inject(repository, "tenantAuthCodeMapper", links);
        inject(repository, "authCodeMapper", codes);
        when(links.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(
                tenantCode(11L), tenantCode(11L), tenantCode(12L),
                tenantCode(13L), tenantCode(14L), tenantCode(15L), tenantCode(16L),
                tenantCode(null)));
        when(codes.selectOneById(11L)).thenReturn(authCode("course:read", 1, 0));
        when(codes.selectOneById(12L)).thenReturn(authCode("platform:usage:read", 1, 0));
        when(codes.selectOneById(13L)).thenReturn(authCode("course:write", 0, 0));
        when(codes.selectOneById(14L)).thenReturn(authCode("course:delete", 1, 1));
        when(codes.selectOneById(16L)).thenReturn(authCode("course:publish", null, 0));

        assertEquals(List.of(11L), org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                repository, "resolveSourceAuthCodeIds", 1L,
                Arrays.asList(11L, 12L, 13L, 14L, 15L, 16L, 999L, null, 11L)));
        assertEquals(List.of(11L), org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                repository, "resolveSourceAuthCodeIds", 1L, null));
    }

    @Test
    void excludesPlatformUsagePermissionFromChildTenantTemplates() throws Exception {
        var repository = new TenantRepositoryImpl();
        var tenantCodes = mock(TenantAuthCodeMapper.class);
        var authCodes = mock(AuthCodeMapper.class);
        inject(repository, "tenantAuthCodeMapper", tenantCodes);
        inject(repository, "authCodeMapper", authCodes);
        var link = new TenantAuthCodeDO();
        link.setAuthCodeId(119L);
        var permission = new AuthCodeDO();
        permission.setCode("platform:usage:read");
        permission.setStatus(1);
        permission.setDelFlag(0);
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(link));
        when(authCodes.selectOneById(119L)).thenReturn(permission);
        assertEquals(List.of(), org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                repository, "resolveSourceAuthCodeIds", 1L, List.of(119L)));
    }
    @Test
    void scopesTenantTreeQueriesAndRejectsMissingTenant() throws Exception {
        TenantMapper mapper = mock(TenantMapper.class);
        TenantDO root = tenant(1L, null);
        TenantDO child = tenant(2L, 1L);
        TenantDO grandchild = tenant(3L, 2L);
        when(mapper.selectAll()).thenReturn(List.of(root, child, grandchild));
        when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(3L);
        when(mapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(root, child));
        TenantRepositoryImpl repository = new TenantRepositoryImpl();
        inject(repository, "tenantMapper", mapper);
        assertEquals(
                List.of(1L, 2L, 3L).size(),
                repository.selectDescendantIds(new TenantId(1L)).size());
        TenantScope.set(new TenantId(1L));
        try {
            assertEquals(
                    java.util.Set.of(2L, 3L),
                    new java.util.HashSet<>(repository.selectDescendantIds(new TenantId(2L))));
        } finally {
            TenantScope.clear();
        }
        assertEquals(1L, repository.selectRootTenantId(new TenantId(3L)));
        assertThrows(IllegalArgumentException.class, () -> repository.selectRootTenantId(null));
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions
                    .when(() -> MapstructUtils.convert(any(List.class), eq(TenantBO.class)))
                    .thenReturn(List.of(new TenantBO()));
            Pair<Long, List<TenantBO>> page =
                    repository.selectPage(new TenantId(1L), null, null, "root", "root", 1);
            assertEquals(3L, page.getLeft());
            assertEquals(1, page.getRight().size());
            Pair<Long, List<TenantBO>> unfiltered =
                    repository.selectPage(new TenantId(1L), 2, 20, " ", " ", null);
            assertEquals(3L, unfiltered.getLeft());
            assertTrue(repository.existsByCode("root", null));
            when(mapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
            assertFalse(repository.existsByCode("root", 1L));
            TenantBO value = new TenantBO();
            TenantDO converted = new TenantDO();
            conversions
                    .when(() -> MapstructUtils.convert(value, TenantDO.class))
                    .thenReturn(converted);
            when(mapper.update(any(TenantDO.class))).thenReturn(1);
            when(mapper.deleteById(1L)).thenReturn(1);
            assertTrue(repository.update(value));
            assertTrue(repository.deleteById(1L));
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.selectPage(null, 1, 10, null, null, null));
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
        inject(repository, "tenantMapper", tenants);
        inject(repository, "userMapper", users);
        inject(repository, "roleMapper", roles);
        inject(repository, "menuMapper", menus);
        inject(repository, "authCodeMapper", authCodes);
        inject(repository, "userScopeRoleMapper", userRoles);
        inject(repository, "roleScopeMenuMapper", roleMenus);
        inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        inject(repository, "tenantMenuMapper", tenantMenus);
        inject(repository, "tenantAuthCodeMapper", tenantCodes);
        TenantDO root = tenant(11L, null);
        when(tenants.selectAll()).thenReturn(List.of(root));
        when(tenants.selectOneById(11L)).thenReturn(root);
        when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        when(userRoles.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of());
        when(users.insertSelective(any(UserDO.class)))
                .thenAnswer(
                        invocation -> {
                            UserDO user = invocation.getArgument(0);
                            user.setId(99L);
                            return 1;
                        });
        when(roles.insertSelective(any(RoleDO.class)))
                .thenAnswer(
                        invocation -> {
                            RoleDO role = invocation.getArgument(0);
                            role.setId(88L);
                            assertEquals(new TenantId(12L), com.shiyu.ai.kernel.context.TenantScope.require());
                            return 1;
                        });
        TenantBO convertedValue = new TenantBO();
        TenantDO converted = tenant(12L, 11L);
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions
                    .when(() -> MapstructUtils.convert(any(List.class), eq(TenantBO.class)))
                    .thenReturn(List.of(convertedValue));
            conversions
                    .when(() -> MapstructUtils.convert(root, TenantBO.class))
                    .thenReturn(convertedValue);
            conversions
                    .when(() -> MapstructUtils.convert(any(TenantBO.class), eq(TenantDO.class)))
                    .thenReturn(converted);
            assertEquals(1, repository.selectAll().size());
            assertEquals(convertedValue, repository.selectById(11L));
            TenantBO input = new TenantBO();
            input.setCode("child");
            input.setName("Child");
            assertEquals(12L, repository.insert(input, new TenantId(11L)).getId());
            com.shiyu.ai.kernel.context.TenantScope.withTenant(new TenantId(12L), () -> {
                repository.initializeTenantSecurity(input, new TenantId(11L));
                return null;
            });
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
        when(menus.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(root, group, leaf));
        when(menus.insertSelective(any(MenuDO.class)))
                .thenAnswer(
                        invocation -> {
                            MenuDO target = invocation.getArgument(0);
                            target.setId(100L + target.getName().length());
                            return 1;
                        });
        Method clone =
                TenantRepositoryImpl.class.getDeclaredMethod(
                        "cloneMenusForTenant", Long.class, Long.class, List.class);
        clone.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Long> cloned =
                (List<Long>) clone.invoke(repository, 10L, 20L, Arrays.asList(3L, 999L, null));
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
        inject(repository, "tenantMapper", tenants);
        inject(repository, "userMapper", users);
        inject(repository, "roleMapper", roles);
        inject(repository, "menuMapper", menus);
        inject(repository, "authCodeMapper", authCodes);
        inject(repository, "userScopeRoleMapper", userRoles);
        inject(repository, "roleScopeMenuMapper", roleMenus);
        inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        inject(repository, "tenantMenuMapper", tenantMenus);
        inject(repository, "tenantAuthCodeMapper", tenantCodes);

        MenuDO root = menu(1L, null, "root");
        MenuDO leaf = menu(2L, 1L, "leaf");
        when(menus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(root, leaf));
        when(menus.insertSelective(any(MenuDO.class)))
                .thenAnswer(
                        invocation -> {
                            MenuDO value = invocation.getArgument(0);
                            value.setId(value.getName().equals("root") ? 101L : 102L);
                            return 1;
                        });
        com.shiyu.ai.iam.implementation.persistence.dataobject.TenantAuthCodeDO relation =
                new com.shiyu.ai.iam.implementation.persistence.dataobject.TenantAuthCodeDO();
        relation.setAuthCodeId(9L);
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class))).thenAnswer(invocation -> {
            assertTrue(com.mybatisflex.core.tenant.TenantManager.isIgnoreTenantCondition(),
                    "source tenant permissions must be read outside the new tenant filter");
            return List.of(relation);
        });
        com.shiyu.ai.iam.implementation.persistence.dataobject.AuthCodeDO code =
                new com.shiyu.ai.iam.implementation.persistence.dataobject.AuthCodeDO();
        code.setId(9L);
        code.setStatus(1);
        code.setDelFlag(0);
        when(authCodes.selectOneById(9L)).thenReturn(code);
        when(tenants.insertSelective(any(TenantDO.class)))
                .thenAnswer(
                        invocation -> {
                            TenantDO value = invocation.getArgument(0);
                            value.setId(20L);
                            return 1;
                        });
        when(roles.insertSelective(any(RoleDO.class)))
                .thenAnswer(
                        invocation -> {
                            RoleDO value = invocation.getArgument(0);
                            value.setId(30L);
                            return 1;
                        });
        when(users.insertSelective(any(UserDO.class)))
                .thenAnswer(
                        invocation -> {
                            UserDO value = invocation.getArgument(0);
                            value.setId(40L);
                            return 1;
                        });

        TenantBO input = new TenantBO();
        input.setCode("child");
        input.setName("Child");
        input.setMenuIds(List.of(2L));
        input.setAuthCodeIds(List.of(9L));
        input.setAdminRoleName("Child Admin");
        input.setAdminUsername("child-admin");
        input.setAdminPassword("secret");
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            TenantDO converted = tenant(20L, 10L);
            conversions
                    .when(() -> MapstructUtils.convert(input, TenantDO.class))
                    .thenReturn(converted);
            TenantBO result = repository.insert(input, new TenantId(10L));
            com.shiyu.ai.kernel.context.TenantScope.withTenant(new TenantId(20L), () -> {
                repository.initializeTenantSecurity(result, new TenantId(10L));
                return null;
            });
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
        inject(repository, "tenantMapper", tenants);
        inject(repository, "userMapper", users);
        inject(repository, "roleMapper", roles);
        inject(repository, "menuMapper", menus);
        inject(repository, "authCodeMapper", authCodes);
        inject(repository, "userScopeRoleMapper", userRoles);
        inject(repository, "roleScopeMenuMapper", roleMenus);
        inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        inject(repository, "tenantMenuMapper", tenantMenus);
        inject(repository, "tenantAuthCodeMapper", tenantCodes);
        TenantDO root = tenant(10L, null);
        when(tenants.selectAll()).thenReturn(List.of(root));
        UserScopeRoleDO userRelation = new UserScopeRoleDO();
        userRelation.setUserId(50L);
        TenantAuthCodeDO codeRelation = new TenantAuthCodeDO();
        codeRelation.setAuthCodeId(60L);
        when(userRoles.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(userRelation));
        when(tenantCodes.selectListByQuery(any(QueryWrapper.class)))
                .thenReturn(List.of(codeRelation));
        when(userRoles.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
        when(tenantCodes.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);

        repository.cascadeDelete(new TenantId(10L));

        verify(users).deleteById(50L);
        verify(authCodes).deleteById(60L);
        verify(tenants).deleteByQuery(any(QueryWrapper.class));
    }

    private static TenantDO tenant(Long id, Long parentId) {
        TenantDO value = new TenantDO();
        value.setId(id);
        value.setParentId(parentId);
        return value;
    }

    private static TenantAuthCodeDO tenantCode(Long authCodeId) {
        TenantAuthCodeDO value = new TenantAuthCodeDO();
        value.setAuthCodeId(authCodeId);
        return value;
    }

    private static AuthCodeDO authCode(String code, Integer status, Integer delFlag) {
        AuthCodeDO value = new AuthCodeDO();
        value.setCode(code);
        value.setStatus(status);
        value.setDelFlag(delFlag);
        return value;
    }

    private static MenuDO menu(Long id, Long parentId, String name) {
        MenuDO value = new MenuDO();
        value.setId(id);
        value.setParentId(parentId);
        value.setName(name);
        value.setCode(name);
        value.setType("MENU");
        value.setStatus(1);
        value.setDelFlag(0);
        return value;
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

package com.shiyu.ai.auth.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.persistence.dataobject.*;
import com.shiyu.ai.auth.persistence.mapper.*;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked"})
class RoleRepositorySecurityTest {
    private static final TenantId TENANT = new TenantId(21);

    @Test
    void coversRoleQueriesLifecycleAndTenantScopeChecks() throws Exception {
        RoleMapper roles = mock(RoleMapper.class); RoleScopeMenuMapper roleMenus = mock(RoleScopeMenuMapper.class);
        MenuMapper menus = mock(MenuMapper.class); UserScopeRoleMapper userRoles = mock(UserScopeRoleMapper.class);
        RoleScopeAuthCodeMapper roleCodes = mock(RoleScopeAuthCodeMapper.class);
        RoleRepositoryImpl repository = new RoleRepositoryImpl();
        inject(repository, "roleMapper", roles); inject(repository, "roleScopeMenuMapper", roleMenus); inject(repository, "menuMapper", menus);
        inject(repository, "userScopeRoleMapper", userRoles); inject(repository, "roleScopeAuthCodeMapper", roleCodes);
        when(roles.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
        when(roles.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(new RoleDO()));
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(any(List.class), eq(RoleBO.class))).thenReturn(List.of(new RoleBO()));
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(RoleBO.class))).thenReturn(new RoleBO());
            conversions.when(() -> MapstructUtils.convert(any(Object.class), eq(RoleDO.class))).thenReturn(new RoleDO());
            assertEquals(1L, repository.selectPage(TENANT, null, null, "admin").getLeft());
            assertEquals(1L, repository.selectPage(TENANT, 2, 5, "").getLeft());
            assertEquals(1, repository.selectAll(TENANT, "1").size());
            assertEquals(1, repository.selectAll(TENANT, null).size());
            assertEquals(1, repository.selectAllByTenant("", TENANT).size());
            assertEquals(1, repository.selectAllByTenant("1", TENANT).size());
            when(roles.selectOneByQuery(any(QueryWrapper.class))).thenReturn(new RoleDO());
            assertNotNull(repository.selectById(2L, TENANT)); assertNull(repository.selectById(null, TENANT));
            assertThrows(IllegalArgumentException.class, () -> repository.selectById(2L, new TenantId(0L)));
            RoleBO role = new RoleBO(); role.setTenantId(21L); role.setName("operator");
            when(roles.insertSelective(any(RoleDO.class))).thenAnswer(i -> { ((RoleDO) i.getArgument(0)).setId(4L); return 1; });
            assertEquals(4L, repository.insert(role).getId());
            RoleBO missingTenant = new RoleBO();
            assertThrows(IllegalArgumentException.class, () -> repository.insert(missingTenant));
            RoleBO zeroTenant = new RoleBO(); zeroTenant.setTenantId(0L);
            assertThrows(IllegalArgumentException.class, () -> repository.insert(zeroTenant));
            RoleBO coded = new RoleBO(); coded.setTenantId(21L); coded.setName("coded");
            RoleDO codedDo = new RoleDO(); codedDo.setCode("existing"); codedDo.setName("coded");
            conversions.when(() -> MapstructUtils.convert(coded, RoleDO.class)).thenReturn(codedDo);
            repository.insert(coded);
            when(roles.updateByQuery(any(RoleDO.class), any(QueryWrapper.class))).thenReturn(1);
            role.setId(4L); assertTrue(repository.update(role)); assertFalse(repository.update(null));
            RoleBO invalidRole = new RoleBO(); invalidRole.setId(4L); assertFalse(repository.update(invalidRole));
            RoleBO zeroRole = new RoleBO(); zeroRole.setId(4L); zeroRole.setTenantId(0L); assertFalse(repository.update(zeroRole));
            when(roles.deleteByQuery(any(QueryWrapper.class))).thenReturn(1);
            assertTrue(repository.deleteRoleAndRelations(4L, TENANT)); assertFalse(repository.deleteRoleAndRelations(null, TENANT));
            assertThrows(IllegalArgumentException.class, () -> repository.deleteRoleAndRelations(4L, null));
            assertThrows(IllegalArgumentException.class, () -> repository.deleteRoleAndRelations(4L, new TenantId(0L)));
            assertTrue(repository.isRoleInScope(4L, TENANT)); assertFalse(repository.isRoleInScope(null, TENANT));
            assertTrue(repository.isRoleOwnedByTenant(4L, TENANT)); assertThrows(IllegalArgumentException.class, () -> repository.isRoleOwnedByTenant(4L, null));
            assertTrue(repository.areMenusInTenantScope(null, List.of(21L)));
            assertTrue(repository.areMenusInTenantScope(List.<Long>of(), List.<Long>of())); assertFalse(repository.areMenusInTenantScope(List.of(1L), List.<Long>of()));
            assertFalse(repository.areMenusInTenantScope(List.of(1L), null));
            assertFalse(repository.areMenusInTenantScope(java.util.Arrays.asList(1L, null), List.of(21L)));
            when(menus.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);
            assertTrue(repository.areMenusInTenantScope(List.of(1L), List.of(21L)));
            assertTrue(repository.selectMenuIdsByRoleIds(TENANT, List.of()).isEmpty());
            RoleScopeMenuDO assignment = new RoleScopeMenuDO(); assignment.setRoleId(4L); assignment.setMenuId(8L);
            when(roleMenus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(assignment));
            assertEquals(Map.of(4L, List.of(8L)), repository.selectMenuIdsByRoleIds(TENANT, List.of(4L)));
            when(roleMenus.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(assignment));
            assertEquals(List.of(8L), repository.selectMenuIdsByRoleId(4L, TENANT, TENANT));
            assertTrue(repository.selectMenuIdsByRoleId(4L, TENANT, new TenantId(22L)).isEmpty());
            repository.insertRoleMenus(4L, TENANT, TENANT, List.of(8L)); repository.insertRoleMenus(4L, TENANT, TENANT, List.of());
            assertThrows(IllegalArgumentException.class,
                    () -> repository.insertRoleMenus(4L, TENANT, new TenantId(22L), List.of(8L)));
            repository.deleteRoleMenus(4L, TENANT, TENANT);
            assertDoesNotThrow(() -> repository.deleteRoleMenus(4L, TENANT, new TenantId(22L)));
        }
        assertThrows(IllegalArgumentException.class, () -> repository.selectAll(null, null));
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
}

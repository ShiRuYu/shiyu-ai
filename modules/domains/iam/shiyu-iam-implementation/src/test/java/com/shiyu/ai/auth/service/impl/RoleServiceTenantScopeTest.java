package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.port.repository.RoleRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.port.repository.UserRepository;
import com.shiyu.ai.auth.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class RoleServiceTenantScopeTest {

    private static final ActorContext ACTOR = new ActorContext(new TenantId(9), new UserId(7), false);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final MenuService menus = mock(MenuService.class);
    private final RoleServiceImpl service = new RoleServiceImpl(roles, assignments, users, tenants, menus);

    @Test
    void missingOrForeignTenantIsDeniedBeforeRepositoryMutation() {
        assertTrue(service.allRolesView(ACTOR, "1", null).isEmpty());
        assertFalse(service.assignUserRoles(ACTOR, 20L, new TenantId(10L), List.of(30L)));
        assertFalse(service.replaceRoleMenus(ACTOR, 20L, null, List.of(1L)));
        verify(roles, never()).insertRoleMenus(any(), any(), any(), any());
        verify(assignments, never()).insert(any());
    }

    @Test
    void assignsAndRemovesOnlyUsersInTheCurrentTenantScope() {
        TenantBO tenant = activeTenant(9L);
        when(tenants.selectById(9L)).thenReturn(tenant);
        when(roles.isRoleOwnedByTenant(20L, new TenantId(9L))).thenReturn(true);
        when(users.isUserInScope(30L, new TenantId(9L))).thenReturn(true);

        assertTrue(service.assignUserRoles(ACTOR, 20L, new TenantId(9L), List.of(30L)));
        verify(assignments).insert(any());
        verify(menus).evictRouteMenuCache(30L);

        assertTrue(service.removeUserRoles(ACTOR, 20L, new TenantId(9L), List.of(30L)));
        verify(assignments).deleteByUserIdRoleIdAndTenantId(30L, 20L, new TenantId(9L));

        when(users.isUserInScope(31L, new TenantId(9L))).thenReturn(false);
        assertFalse(service.assignUserRoles(ACTOR, 20L, new TenantId(9L), List.of(31L)));
    }

    @Test
    void validatesMenusAndEvictsRouteCacheOnlyAfterSuccessfulReplacement() {
        when(tenants.selectById(9L)).thenReturn(activeTenant(9L));
        when(roles.isRoleOwnedByTenant(20L, new TenantId(9L))).thenReturn(true);
        when(roles.areMenusInTenantScope(List.of(1L, 2L, 2L), List.of(9L))).thenReturn(true);

        assertTrue(service.replaceRoleMenus(ACTOR, 20L, new TenantId(9L), List.of(1L, 2L, 2L)));
        verify(roles).deleteRoleMenus(20L, new TenantId(9L), new TenantId(9L));
        verify(roles).insertRoleMenus(20L, new TenantId(9L), new TenantId(9L), List.of(1L, 2L));
        verify(menus).evictAllRouteMenuCache();

        when(roles.areMenusInTenantScope(List.of(3L), List.of(9L))).thenReturn(false);
        assertFalse(service.replaceRoleMenus(ACTOR, 20L, new TenantId(9L), List.of(3L)));
    }

    @Test
    void deletesRoleOnlyWhenItIsInScope() {
        when(roles.isRoleInScope(20L, new TenantId(9L))).thenReturn(false).thenReturn(true);
        when(roles.deleteRoleAndRelations(20L, new TenantId(9L))).thenReturn(true);
        assertFalse(service.deleteRole(ACTOR, 20L));
        assertTrue(service.deleteRole(ACTOR, 20L));
        verify(roles).deleteRoleAndRelations(20L, new TenantId(9L));
        verify(menus).evictAllRouteMenuCache();
    }

    @Test
    void keepsRoleListTenantScopedAndAddsMenuPermissionsInBatch() {
        RoleBO role = new RoleBO();
        role.setId(20L);
        when(roles.selectPage(ACTOR.tenantId(), 1, 10, "admin"))
                .thenReturn(Pair.of(1L, List.of(role)));
        when(roles.selectMenuIdsByRoleIds(ACTOR.tenantId(), List.of(20L)))
                .thenReturn(java.util.Map.of(20L, List.of(99L)));

        try (MockedStatic<com.shiyu.ai.common.core.utils.MapstructUtils> mapper =
                     org.mockito.Mockito.mockStatic(com.shiyu.ai.common.core.utils.MapstructUtils.class)) {
            mapper.when(() -> com.shiyu.ai.common.core.utils.MapstructUtils.convert(any(List.class),
                    eq(com.shiyu.ai.auth.vo.RoleVO.class)))
                    .thenReturn(List.of(mock(com.shiyu.ai.auth.vo.RoleVO.class)));
            service.getRoleList(ACTOR, 1, 10, "admin");
        }
        assertTrue(role.getPermissions().contains(99L));
        verify(roles).selectMenuIdsByRoleIds(ACTOR.tenantId(), List.of(20L));
    }

    private static TenantBO activeTenant(Long id) {
        TenantBO tenant = new TenantBO();
        tenant.setId(id);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        return tenant;
    }
}

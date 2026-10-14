package com.shiyu.ai.iam.implementation.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.port.repository.RoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.service.MenuService;
import com.shiyu.ai.iam.implementation.request.RoleRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** 验证角色变更在租户边界内执行，并拒绝越权修改。 */
class RoleMutationBoundaryTest {
    private static final TenantId TENANT = new TenantId(9);
    private static final ActorContext ACTOR = new ActorContext(TENANT, new UserId(7), false);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final MenuService menus = mock(MenuService.class);
    private final RoleServiceImpl service = new RoleServiceImpl(roles, assignments, users, tenants, menus);

    @Test
    void roleDetailRejectsRoleChangedAfterOwnershipCheck() {
        allowRoleAndTenant();
        RoleBO role = new RoleBO();
        role.setId(20L);
        role.setTenantId(10L);
        role.setStatus(1);
        role.setDelFlag(0);
        when(roles.selectById(20L, TENANT)).thenReturn(role);

        assertNull(service.detailView(ACTOR, 20L, TENANT));
        role.setTenantId(9L);
        role.setStatus(0);
        assertNull(service.detailView(ACTOR, 20L, TENANT));
        role.setStatus(1);
        role.setDelFlag(1);
        assertNull(service.detailView(ACTOR, 20L, TENANT));
        role.setDelFlag(0);
        role.setId(21L);
        assertNull(service.detailView(ACTOR, 20L, TENANT));
        verify(roles, never()).selectMenuIdsByRoleId(any(), any(), any());
    }

    @Test
    void rejectsWholeAssignmentBatchBeforeGrantingAnyRoleWhenLaterUserIsOutOfScope() {
        allowRoleAndTenant();
        when(users.isUserInScope(30L, TENANT)).thenReturn(true);
        when(users.isUserInScope(31L, TENANT)).thenReturn(false);

        assertFalse(service.assignUserRoles(ACTOR, 20L, TENANT, List.of(30L, 31L)));
        verify(assignments, never()).insert(any());
        verify(menus, never()).evictRouteMenuCache(any());
    }

    @Test
    void rejectsWholeRemovalBatchBeforeRevokingAnyRoleWhenLaterUserIsOutOfScope() {
        allowRoleAndTenant();
        when(users.isUserInScope(30L, TENANT)).thenReturn(true);
        when(users.isUserInScope(31L, TENANT)).thenReturn(false);

        assertFalse(service.removeUserRoles(ACTOR, 20L, TENANT, List.of(30L, 31L)));
        verify(assignments, never()).deleteByUserIdRoleIdAndTenantId(any(), any(), any());
        verify(menus, never()).evictRouteMenuCache(any());
    }

    @Test
    void rejectsRoleAssignmentAndRemovalOutsideTheActorsTenant() {
        TenantId sibling = new TenantId(10);
        TenantBO siblingTenant = new TenantBO();
        siblingTenant.setId(10L);
        siblingTenant.setStatus(1);
        siblingTenant.setDelFlag(0);
        when(tenants.selectById(10L)).thenReturn(siblingTenant);
        when(roles.isRoleOwnedByTenant(20L, sibling)).thenReturn(true);
        when(users.isUserInScope(30L, TENANT)).thenReturn(true);

        assertFalse(service.assignUserRoles(ACTOR, 20L, sibling, List.of(30L)));
        assertFalse(service.removeUserRoles(ACTOR, 20L, sibling, List.of(30L)));
        verify(assignments, never()).insert(any());
        verify(assignments, never()).deleteByUserIdRoleIdAndTenantId(any(), any(), any());
    }

    @Test
    void refusesMutationsWhenTheTargetTenantOrRoleWasRevoked() {
        allowRoleAndTenant();
        TenantBO disabledTenant = new TenantBO();
        disabledTenant.setId(9L);
        disabledTenant.setStatus(0);
        when(tenants.selectById(9L)).thenReturn(disabledTenant);
        assertFalse(service.assignUserRoles(ACTOR, 20L, TENANT, List.of(30L)));
        assertFalse(service.removeUserRoles(ACTOR, 20L, TENANT, List.of(30L)));

        disabledTenant.setStatus(1);
        when(roles.isRoleOwnedByTenant(20L, TENANT)).thenReturn(false);
        assertFalse(service.assignUserRoles(ACTOR, 20L, TENANT, List.of(30L)));
        assertFalse(service.removeUserRoles(ACTOR, 20L, TENANT, List.of(30L)));
        verify(assignments, never()).insert(any());
        verify(assignments, never()).deleteByUserIdRoleIdAndTenantId(any(), any(), any());
    }

    @Test
    void rejectsInvalidMenusBeforeCreatingRole() {
        allowRoleAndTenant();
        RoleRequest request = new RoleRequest();
        RoleBO role = new RoleBO();
        role.setTenantId(9L);
        role.setPermissions(List.of(99L));
        when(roles.areMenusInTenantScope(List.of(99L), List.of(9L))).thenReturn(false);
        try (MockedStatic<MapstructUtils> mapper = org.mockito.Mockito.mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, RoleBO.class)).thenReturn(role);
            assertThrows(IllegalArgumentException.class, () -> service.createRole(ACTOR, request));
        }
        verify(roles, never()).insert(any());
    }

    @Test
    void rejectsInvalidMenusBeforeUpdatingRole() {
        allowRoleAndTenant();
        RoleRequest request = new RoleRequest();
        RoleBO role = new RoleBO();
        role.setTenantId(9L);
        role.setPermissions(List.of(99L));
        when(roles.selectById(20L, TENANT)).thenReturn(role);
        when(roles.update(any())).thenReturn(true);
        when(roles.areMenusInTenantScope(List.of(99L), List.of(9L))).thenReturn(false);
        try (MockedStatic<MapstructUtils> mapper = org.mockito.Mockito.mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, RoleBO.class)).thenReturn(role);
            assertThrows(IllegalArgumentException.class, () -> service.updateRole(ACTOR, 20L, request));
        }
        verify(roles, never()).update(any());
    }

    private void allowRoleAndTenant() {
        TenantBO tenant = new TenantBO();
        tenant.setId(9L);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        when(tenants.selectById(9L)).thenReturn(tenant);
        when(roles.isRoleOwnedByTenant(20L, TENANT)).thenReturn(true);
    }
}

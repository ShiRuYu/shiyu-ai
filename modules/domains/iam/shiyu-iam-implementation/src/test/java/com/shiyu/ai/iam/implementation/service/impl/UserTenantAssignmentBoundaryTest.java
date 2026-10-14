package com.shiyu.ai.iam.implementation.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.utils.MapstructUtils;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.port.repository.RoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.service.MenuService;
import com.shiyu.ai.iam.implementation.request.UserRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/** 验证用户租户角色分配仅在授权的租户范围内生效。 */
class UserTenantAssignmentBoundaryTest {
    @Test
    void delegatedActorCannotManageAnotherHomeTenantUserFromChildTenant() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        UserServiceImpl service = new UserServiceImpl(
                users, mock(RoleRepository.class), assignments, tenants,
                mock(TenantRoleRepository.class), mock(MenuService.class));
        ActorContext delegated = new ActorContext(
                new TenantId(8L), new UserId(11L), null, "tenant_super",
                new TenantId(7L), "PARENT_SUPER_ADMIN", false);
        when(users.isUserInScope(20L, new TenantId(8L))).thenReturn(false);
        when(users.isUserInScope(20L, new TenantId(7L))).thenReturn(true);

        assertNull(service.resetUserPassword(delegated, 20L, "changed"));
        assertTrue(service.getTenantAssignments(delegated, 20L).isEmpty());
        assertFalse(service.replaceTenantAssignments(delegated, 20L, List.of()));
        verify(users, never()).selectById(20L);
        verify(users, never()).update(any());
        verifyNoInteractions(assignments, tenants);
    }

    @Test
    void rejectsInvalidRoleBeforeCreatingUser() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(users, roles,
                mock(UserScopeRoleRepository.class), mock(TenantRepository.class),
                mock(TenantRoleRepository.class), mock(MenuService.class));
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(11), false);
        UserRequest request = new UserRequest();
        UserBO user = new UserBO();
        user.setUsername("new-user");
        when(roles.isRoleOwnedByTenant(30L, new TenantId(7))).thenReturn(false);
        try (MockedStatic<MapstructUtils> mapper = org.mockito.Mockito.mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, UserBO.class)).thenReturn(user);
            assertThrows(IllegalArgumentException.class,
                    () -> service.createUser(actor, request, new Long[] {30L}, 7L));
        }
        verify(users, never()).insert(any());
    }

    @Test
    void rejectsInvalidRoleBeforeUpdatingUserProfile() {
        UserRepository users = mock(UserRepository.class);
        RoleRepository roles = mock(RoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(users, roles,
                mock(UserScopeRoleRepository.class), mock(TenantRepository.class),
                mock(TenantRoleRepository.class), mock(MenuService.class));
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(11), false);
        UserRequest request = new UserRequest();
        UserBO patch = new UserBO();
        UserBO existing = new UserBO();
        existing.setId(20L);
        existing.setPassword("encoded");
        when(users.isUserInScope(20L, new TenantId(7))).thenReturn(true);
        when(users.selectById(20L)).thenReturn(existing);
        when(users.update(any())).thenReturn(true);
        when(roles.isRoleOwnedByTenant(30L, new TenantId(7))).thenReturn(false);
        try (MockedStatic<MapstructUtils> mapper = org.mockito.Mockito.mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, UserBO.class)).thenReturn(patch);
            assertThrows(IllegalArgumentException.class,
                    () -> service.updateUser(actor, 20L, request, new Long[] {30L}, 7L));
        }
        verify(users, never()).update(any());
    }

    @Test
    void omitsAssignmentWhoseRoleBelongsToAnotherTenant() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        UserServiceImpl service = new UserServiceImpl(
                users, mock(RoleRepository.class), assignments, tenants, tenantRoles,
                mock(MenuService.class));
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(11), false);
        UserScopeRoleBO grant = new UserScopeRoleBO();
        grant.setUserId(20L);
        grant.setTenantId(7L);
        grant.setRoleId(30L);
        grant.setStatus(1);
        grant.setDelFlag(0);
        TenantBO tenant = new TenantBO();
        tenant.setId(7L);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        RoleBO foreignRole = new RoleBO();
        foreignRole.setId(30L);
        foreignRole.setTenantId(8L);
        foreignRole.setStatus(1);
        foreignRole.setDelFlag(0);
        when(users.isUserInScope(20L, new TenantId(7L))).thenReturn(true);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L));
        when(assignments.selectByUserId(20L)).thenReturn(List.of(grant));
        when(tenantRoles.selectTenantById(new TenantId(7L))).thenReturn(tenant);
        when(tenantRoles.selectRoleById(30L)).thenReturn(foreignRole);

        assertTrue(service.getTenantAssignments(actor, 20L).isEmpty());
        foreignRole.setTenantId(7L);
        grant.setStatus(null);
        assertTrue(service.getTenantAssignments(actor, 20L).isEmpty());
    }
}

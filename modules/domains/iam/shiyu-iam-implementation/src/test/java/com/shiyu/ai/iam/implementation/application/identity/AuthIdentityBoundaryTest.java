package com.shiyu.ai.iam.implementation.application.identity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.service.MenuService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.List;

import org.junit.jupiter.api.Test;

/** 验证身份切换仅接受当前租户的有效角色和可用的子租户委派角色。 */
class AuthIdentityBoundaryTest {
    @Test
    void failedTenantContextPersistenceDoesNotEvictMenusOrReportSuccess() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        MenuService menus = mock(MenuService.class);
        AuthIdentityUseCase identity = new AuthIdentityUseCase(
                users, assignments, tenantRoles, tenants, menus,
                new AuthTenantContextSupport(tenants, tenantRoles));
        UserBO user = new UserBO();
        user.setId(11L);
        user.setExtInfo("{\"homeTenantId\":7,\"currentTenantId\":7}");
        UserScopeRoleBO grant = new UserScopeRoleBO();
        grant.setUserId(11L);
        grant.setTenantId(8L);
        grant.setRoleId(31L);
        grant.setStatus(1);
        grant.setDelFlag(0);
        RoleBO role = new RoleBO();
        role.setId(31L);
        role.setTenantId(8L);
        role.setCode("member");
        role.setStatus(1);
        role.setDelFlag(0);
        TenantBO target = new TenantBO();
        target.setId(8L);
        target.setStatus(1);
        target.setDelFlag(0);
        when(users.selectById(11L)).thenReturn(user);
        when(assignments.selectByUserId(11L)).thenReturn(List.of(grant));
        when(tenantRoles.selectTenantById(new TenantId(8L))).thenReturn(target);
        when(tenantRoles.selectRoleById(31L)).thenReturn(role);
        when(users.update(user)).thenReturn(false);

        assertFalse(identity.switchCurrentTenant(11L, new TenantId(8L)));
        verify(users).update(user);
        verifyNoInteractions(menus);
    }

    @Test
    void tenantListUsesActorHomeWhenStoredContextOmitsIt() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        AuthIdentityUseCase identity = new AuthIdentityUseCase(
                users, assignments, tenantRoles, tenants, mock(MenuService.class),
                new AuthTenantContextSupport(tenants, tenantRoles));
        UserBO user = new UserBO();
        user.setId(11L);
        user.setExtInfo("{}");
        UserScopeRoleBO grant = new UserScopeRoleBO();
        grant.setUserId(11L);
        grant.setTenantId(7L);
        grant.setRoleId(30L);
        grant.setStatus(1);
        grant.setDelFlag(0);
        RoleBO homeSuper = new RoleBO();
        homeSuper.setId(30L);
        homeSuper.setTenantId(7L);
        homeSuper.setCode("tenant_super");
        homeSuper.setStatus(1);
        homeSuper.setDelFlag(0);
        TenantBO home = new TenantBO();
        home.setId(7L);
        home.setName("Home");
        home.setStatus(1);
        home.setDelFlag(0);
        TenantBO child = new TenantBO();
        child.setId(8L);
        child.setName("Child");
        child.setParentId(7L);
        child.setStatus(1);
        child.setDelFlag(0);
        when(assignments.selectByUserId(11L)).thenReturn(List.of(grant));
        when(tenantRoles.selectRoleById(30L)).thenReturn(homeSuper);
        when(tenantRoles.selectTenantById(new TenantId(7L))).thenReturn(home);
        when(users.selectById(11L)).thenReturn(user);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L, 8L));
        when(tenants.selectAll()).thenReturn(List.of(home, child));
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(11L), false);

        assertEquals(java.util.Set.of(7L, 8L),
                identity.getUserTenants(actor, 11L).stream()
                        .map(com.shiyu.ai.iam.implementation.vo.TenantInfoVO::getId)
                        .collect(java.util.stream.Collectors.toSet()));
    }
    @Test
    void rejectsTenantSwitchWhenAssignmentPointsToForeignOrDisabledRole() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        AuthIdentityUseCase identity = new AuthIdentityUseCase(
                users, assignments, tenantRoles, tenants, mock(MenuService.class),
                new AuthTenantContextSupport(tenants, tenantRoles));
        UserBO user = new UserBO();
        user.setId(11L);
        user.setExtInfo("{\"homeTenantId\":7,\"currentTenantId\":7}");
        UserScopeRoleBO grant = new UserScopeRoleBO();
        grant.setUserId(11L);
        grant.setTenantId(8L);
        grant.setRoleId(31L);
        grant.setStatus(1);
        grant.setDelFlag(0);
        TenantBO target = new TenantBO();
        target.setId(8L);
        target.setStatus(1);
        target.setDelFlag(0);
        RoleBO role = new RoleBO();
        role.setId(31L);
        role.setTenantId(99L);
        role.setStatus(1);
        role.setDelFlag(0);
        when(users.selectById(11L)).thenReturn(user);
        when(assignments.selectByUserId(11L)).thenReturn(List.of(grant));
        when(tenantRoles.selectTenantById(new TenantId(8L))).thenReturn(target);
        when(tenantRoles.selectRoleById(31L)).thenReturn(role);

        assertFalse(identity.switchCurrentTenant(11L, new TenantId(8L)));
        role.setTenantId(8L);
        role.setStatus(0);
        assertFalse(identity.switchCurrentTenant(11L, new TenantId(8L)));
        verify(users, never()).update(any());
    }

    @Test
    void refusesRoleSwitchAfterTheAssignedRoleIsDisabledOrDeleted() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        AuthIdentityUseCase identity = new AuthIdentityUseCase(
                users, assignments, tenantRoles, tenants, mock(MenuService.class),
                new AuthTenantContextSupport(tenants, tenantRoles));
        UserBO user = new UserBO();
        user.setId(11L);
        user.setExtInfo("{\"homeTenantId\":7,\"currentTenantId\":7}");
        UserScopeRoleBO grant = new UserScopeRoleBO();
        grant.setUserId(11L);
        grant.setTenantId(7L);
        grant.setRoleId(30L);
        grant.setStatus(1);
        grant.setDelFlag(0);
        UserScopeRoleBO liveGrant = new UserScopeRoleBO();
        liveGrant.setUserId(11L);
        liveGrant.setTenantId(7L);
        liveGrant.setRoleId(29L);
        liveGrant.setStatus(1);
        liveGrant.setDelFlag(0);
        RoleBO liveRole = new RoleBO();
        liveRole.setId(29L);
        liveRole.setTenantId(7L);
        liveRole.setStatus(1);
        liveRole.setDelFlag(0);
        RoleBO role = new RoleBO();
        role.setId(30L);
        role.setTenantId(7L);
        role.setStatus(0);
        role.setDelFlag(0);
        when(users.selectById(11L)).thenReturn(user);
        when(assignments.selectByUserId(11L)).thenReturn(List.of(liveGrant, grant));
        when(tenantRoles.selectRoleById(29L)).thenReturn(liveRole);
        when(users.selectRolesByUserId(11L)).thenReturn(List.of(role));

        assertFalse(identity.switchCurrentRole(11L, 30L));
        role.setStatus(1);
        role.setDelFlag(1);
        assertFalse(identity.switchCurrentRole(11L, 30L));
        verify(users, never()).update(any());
    }

    @Test
    void refusesDelegationWhenChildHasNoActiveTenantSuperRole() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        AuthIdentityUseCase identity = new AuthIdentityUseCase(
                users, assignments, tenantRoles, tenants, mock(MenuService.class),
                new AuthTenantContextSupport(tenants, tenantRoles));
        UserBO user = new UserBO();
        user.setId(11L);
        user.setExtInfo("{\"homeTenantId\":7,\"currentTenantId\":7}");
        UserScopeRoleBO homeGrant = new UserScopeRoleBO();
        homeGrant.setUserId(11L);
        homeGrant.setTenantId(7L);
        homeGrant.setRoleId(30L);
        homeGrant.setStatus(1);
        homeGrant.setDelFlag(0);
        RoleBO homeSuper = new RoleBO();
        homeSuper.setId(30L);
        homeSuper.setTenantId(7L);
        homeSuper.setCode("tenant_super");
        homeSuper.setStatus(1);
        homeSuper.setDelFlag(0);
        TenantBO child = new TenantBO();
        child.setId(8L);
        child.setStatus(1);
        child.setDelFlag(0);
        when(users.selectById(11L)).thenReturn(user);
        when(assignments.selectByUserId(11L)).thenReturn(List.of(homeGrant));
        when(tenantRoles.selectRoleById(30L)).thenReturn(homeSuper);
        when(tenantRoles.selectTenantById(new TenantId(8L))).thenReturn(child);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of(7L, 8L));
        when(users.update(user)).thenReturn(true);

        assertFalse(identity.switchCurrentTenant(11L, new TenantId(8L)));
        RoleBO foreignSuper = new RoleBO();
        foreignSuper.setId(31L);
        foreignSuper.setTenantId(99L);
        foreignSuper.setCode("tenant_super");
        foreignSuper.setStatus(1);
        foreignSuper.setDelFlag(0);
        when(tenantRoles.selectTenantSuperRole(new TenantId(8L))).thenReturn(foreignSuper);
        assertFalse(identity.switchCurrentTenant(11L, new TenantId(8L)));
        verify(users, never()).update(any());
    }

    @Test
    void rejectsRoleSwitchWhenResolvedRoleBelongsToAnotherTenant() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        AuthIdentityUseCase identity = new AuthIdentityUseCase(
                users, assignments, tenantRoles, tenants, mock(MenuService.class),
                new AuthTenantContextSupport(tenants, tenantRoles));
        UserBO user = new UserBO();
        user.setId(11L);
        user.setExtInfo("{\"homeTenantId\":7,\"currentTenantId\":7}");
        UserScopeRoleBO grant = new UserScopeRoleBO();
        grant.setUserId(11L);
        grant.setTenantId(7L);
        grant.setRoleId(30L);
        grant.setStatus(1);
        grant.setDelFlag(0);
        UserScopeRoleBO liveGrant = new UserScopeRoleBO();
        liveGrant.setUserId(11L);
        liveGrant.setTenantId(7L);
        liveGrant.setRoleId(29L);
        liveGrant.setStatus(1);
        liveGrant.setDelFlag(0);
        RoleBO liveRole = new RoleBO();
        liveRole.setId(29L);
        liveRole.setTenantId(7L);
        liveRole.setStatus(1);
        liveRole.setDelFlag(0);
        RoleBO foreignRole = new RoleBO();
        foreignRole.setId(30L);
        foreignRole.setTenantId(8L);
        foreignRole.setStatus(1);
        foreignRole.setDelFlag(0);
        when(users.selectById(11L)).thenReturn(user);
        when(assignments.selectByUserId(11L)).thenReturn(List.of(liveGrant, grant));
        when(tenantRoles.selectRoleById(29L)).thenReturn(liveRole);
        when(users.selectRolesByUserId(11L)).thenReturn(List.of(foreignRole));

        assertFalse(identity.switchCurrentRole(11L, 30L));
        verify(users, never()).update(any());
    }

    @Test
    void doesNotReadAnotherUsersTenantMemberships() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        AuthIdentityUseCase identity = new AuthIdentityUseCase(
                users, assignments, tenantRoles, tenants, mock(MenuService.class),
                new AuthTenantContextSupport(tenants, tenantRoles));
        ActorContext actor = new ActorContext(new TenantId(7), new UserId(11), false);

        assertTrue(identity.getUserTenants(actor, 20L).isEmpty());
        verifyNoInteractions(users, assignments, tenantRoles, tenants);
    }
}

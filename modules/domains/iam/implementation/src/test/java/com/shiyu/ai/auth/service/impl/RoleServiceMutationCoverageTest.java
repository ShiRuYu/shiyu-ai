package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.port.repository.RoleRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.port.repository.UserRepository;
import com.shiyu.ai.auth.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.auth.request.RoleRequest;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoleServiceMutationCoverageTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(9L), new UserId(7L), false);

    private final RoleRepository roles = mock(RoleRepository.class);
    private final UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final MenuService menus = mock(MenuService.class);
    private final RoleServiceImpl service = new RoleServiceImpl(roles, assignments, users, tenants, menus);

    @Test
    void createsAndUpdatesRoleAndPersistsValidatedMenuAssignments() {
        when(tenants.selectById(9L)).thenReturn(activeTenant(9L));
        when(roles.areMenusInTenantScope(List.of(1L, 2L), List.of(9L))).thenReturn(true);
        when(roles.insert(any(RoleBO.class))).thenAnswer(invocation -> {
            RoleBO role = invocation.getArgument(0);
            role.setId(50L);
            return role;
        });

        RoleRequest request = request(9L, List.of(1L, 2L));
        RoleBO mapped = role(null, 9L);
        mapped.setPermissions(List.of(1L, 2L));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, RoleBO.class)).thenReturn(mapped);
            assertTrue(service.createRole(ACTOR, request));
            verify(roles).insertRoleMenus(50L, new TenantId(9L), new TenantId(9L), List.of(1L, 2L));
            verify(menus).evictAllRouteMenuCache();

            RoleBO existing = role(50L, 9L);
            when(roles.selectById(50L, new TenantId(9L))).thenReturn(existing);
            when(roles.update(any(RoleBO.class))).thenReturn(true);
            RoleBO update = role(null, 9L);
            update.setPermissions(List.of(1L, 2L));
            mapper.when(() -> MapstructUtils.convert(request, RoleBO.class)).thenReturn(update);
            assertTrue(service.updateRole(ACTOR, 50L, request));
            verify(roles).deleteRoleMenus(50L, new TenantId(9L), new TenantId(9L));
            verify(roles, times(2)).insertRoleMenus(50L, new TenantId(9L), new TenantId(9L), List.of(1L, 2L));
        }
    }

    @Test
    void rejectsInvalidCreateUpdateScopesAndMenuAssignments() {
        RoleRequest missingTenant = request(null, List.of());
        RoleRequest empty = request(9L, List.of());
        RoleRequest invalidMenu = request(9L, List.of(8L));
        RoleRequest foreign = request(10L, List.of());
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(missingTenant, RoleBO.class)).thenReturn(role(null, null));
            assertFalse(service.createRole(ACTOR, missingTenant));
            when(tenants.selectById(9L)).thenReturn(inactiveTenant(9L));
            mapper.when(() -> MapstructUtils.convert(empty, RoleBO.class)).thenReturn(role(null, 9L));
            assertFalse(service.createRole(ACTOR, empty));

            when(tenants.selectById(9L)).thenReturn(activeTenant(9L));
            when(roles.areMenusInTenantScope(List.of(8L), List.of(9L))).thenReturn(false);
            RoleBO invalidRole = role(null, 9L);
            invalidRole.setPermissions(List.of(8L));
            mapper.when(() -> MapstructUtils.convert(invalidMenu, RoleBO.class)).thenReturn(invalidRole);
            assertThrows(IllegalArgumentException.class, () -> service.createRole(ACTOR, invalidMenu));

            mapper.when(() -> MapstructUtils.convert((RoleRequest) null, RoleBO.class)).thenReturn(null);
            assertFalse(service.updateRole(ACTOR, 50L, null));
            when(roles.selectById(50L, new TenantId(9L))).thenReturn(null);
            mapper.when(() -> MapstructUtils.convert(empty, RoleBO.class)).thenReturn(role(null, 9L));
            assertFalse(service.updateRole(ACTOR, 50L, empty));
            when(roles.selectById(50L, new TenantId(9L))).thenReturn(role(50L, 9L));
            when(tenants.selectById(10L)).thenReturn(activeTenant(10L));
            mapper.when(() -> MapstructUtils.convert(foreign, RoleBO.class)).thenReturn(role(null, 10L));
            assertFalse(service.updateRole(ACTOR, 50L, foreign));
            verify(roles, never()).update(any(RoleBO.class));
        }
    }

    @Test
    void listsAndLoadsRolesOnlyWithinAssignableTenantScope() {
        when(tenants.selectById(9L)).thenReturn(activeTenant(9L));
        RoleBO current = role(1L, 9L);
        RoleBO malformed = role(2L, null);
        when(roles.selectAllByTenant("1", new TenantId(9L))).thenReturn(List.of(current, malformed));
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(anyList(), eq(com.shiyu.ai.auth.vo.RoleVO.class)))
                    .thenAnswer(invocation -> ((List<?>) invocation.getArgument(0)).isEmpty()
                            ? List.of() : List.of(mock(com.shiyu.ai.auth.vo.RoleVO.class)));
            assertEquals(1, service.allRolesView(ACTOR, "1", new TenantId(9L)).size());
            assertTrue(service.allRolesView(ACTOR, "1", null).isEmpty());

            when(roles.isRoleOwnedByTenant(1L, new TenantId(9L))).thenReturn(true);
            when(roles.selectById(1L, new TenantId(9L))).thenReturn(current);
            when(roles.selectMenuIdsByRoleId(1L, new TenantId(9L), new TenantId(9L))).thenReturn(List.of(4L));
            mapper.when(() -> MapstructUtils.convert(current, com.shiyu.ai.auth.vo.RoleVO.class))
                    .thenReturn(mock(com.shiyu.ai.auth.vo.RoleVO.class));
            assertNotNull(service.detailView(ACTOR, 1L, new TenantId(9L)));
            when(roles.isRoleOwnedByTenant(2L, new TenantId(9L))).thenReturn(false);
            assertNull(service.detailView(ACTOR, 2L, new TenantId(9L)));

            ActorContext platform = new ActorContext(new TenantId(9L), new UserId(7L), true);
            when(tenants.selectById(11L)).thenReturn(activeTenant(11L));
            when(tenants.selectDescendantIds(new TenantId(9L))).thenReturn(List.of(11L));
            when(roles.selectAllByTenant(null, new TenantId(11L))).thenReturn(List.of(role(3L, 11L)));
            assertEquals(1, service.allRolesView(platform, null, new TenantId(11L)).size());
        }
    }

    @Test
    void handlesEmptyAssignmentsAndStopsBeforeMutationForForeignUsers() {
        assertTrue(service.assignUserRoles(ACTOR, 1L, new TenantId(9L), List.of()));
        assertTrue(service.removeUserRoles(ACTOR, 1L, new TenantId(9L), null));
        when(tenants.selectById(9L)).thenReturn(activeTenant(9L));
        when(roles.isRoleOwnedByTenant(1L, new TenantId(9L))).thenReturn(true);
        when(users.isUserInScope(7L, new TenantId(9L))).thenReturn(false);
        assertFalse(service.assignUserRoles(ACTOR, 1L, new TenantId(9L), List.of(7L)));
        assertFalse(service.removeUserRoles(ACTOR, 1L, new TenantId(9L), List.of(7L)));
        verify(assignments, never()).insert(any());
        verify(assignments, never()).deleteByUserIdRoleIdAndTenantId(anyLong(), anyLong(), any(TenantId.class));
    }

    @Test
    void coversInactiveDeletedAndCrossTenantRoleScopeDecisions() {
        // Tenant status and deletion flags are both hard authorization stops.
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(anyList(), eq(com.shiyu.ai.auth.vo.RoleVO.class)))
                    .thenAnswer(invocation -> ((List<?>) invocation.getArgument(0)).stream()
                            .map(value -> mock(com.shiyu.ai.auth.vo.RoleVO.class)).toList());
            when(tenants.selectById(9L)).thenReturn(null);
            assertTrue(service.allRolesView(ACTOR, null, new TenantId(9L)).isEmpty());
            when(tenants.selectById(9L)).thenReturn(inactiveTenant(9L));
            assertTrue(service.allRolesView(ACTOR, null, new TenantId(9L)).isEmpty());
            TenantBO deleted = activeTenant(9L);
            deleted.setDelFlag(1);
            when(tenants.selectById(9L)).thenReturn(deleted);
            assertTrue(service.allRolesView(ACTOR, null, new TenantId(9L)).isEmpty());

            // A normal user cannot target a sibling/descendant tenant.
            when(tenants.selectById(10L)).thenReturn(activeTenant(10L));
            when(tenants.selectDescendantIds(new TenantId(9L))).thenReturn(List.of(10L));
            assertTrue(service.allRolesView(ACTOR, null, new TenantId(10L)).isEmpty());

            // Platform and parent-super-admin actors may target descendants.
            ActorContext parentAdmin = new ActorContext(new TenantId(9L), new UserId(7L),
                    null, null, new TenantId(9L), "PARENT_SUPER_ADMIN", false);
            when(roles.selectAllByTenant(null, new TenantId(10L))).thenReturn(List.of(role(2L, 10L)));
            assertEquals(1, service.allRolesView(parentAdmin, null, new TenantId(10L)).size());
        }

        // Replacement accepts null/empty menu lists after scope validation but
        // must not manufacture an insert operation.
        when(tenants.selectById(9L)).thenReturn(activeTenant(9L));
        when(roles.isRoleOwnedByTenant(20L, new TenantId(9L))).thenReturn(true);
        when(roles.areMenusInTenantScope(List.of(), List.of(9L))).thenReturn(true);
        assertTrue(service.replaceRoleMenus(ACTOR, 20L, new TenantId(9L), null));
        assertTrue(service.replaceRoleMenus(ACTOR, 20L, new TenantId(9L), List.of()));
            verify(roles, never()).insertRoleMenus(eq(20L), eq(new TenantId(9L)), eq(new TenantId(9L)), eq(List.of()));
    }

    @Test
    void coversMutationFalseAndMissingRoleBranches() {
        when(tenants.selectById(9L)).thenReturn(activeTenant(9L));
        when(roles.isRoleOwnedByTenant(20L, new TenantId(9L))).thenReturn(false);
        assertFalse(service.replaceRoleMenus(ACTOR, 20L, new TenantId(9L), List.of(1L)));

        when(roles.selectById(50L, new TenantId(9L))).thenReturn(role(50L, 9L));
        RoleRequest request = request(9L, List.of());
        RoleBO mapped = role(null, 9L);
        try (MockedStatic<MapstructUtils> mapper = mockStatic(MapstructUtils.class)) {
            mapper.when(() -> MapstructUtils.convert(request, RoleBO.class)).thenReturn(mapped);
            when(roles.update(any(RoleBO.class))).thenReturn(false);
            assertFalse(service.updateRole(ACTOR, 50L, request));
            verify(roles, never()).deleteRoleMenus(anyLong(), any(TenantId.class), any(TenantId.class));
        }

        when(roles.isRoleInScope(60L, new TenantId(9L))).thenReturn(true);
        when(roles.deleteRoleAndRelations(60L, new TenantId(9L))).thenReturn(false);
        assertFalse(service.deleteRole(ACTOR, 60L));
    }

    private static RoleRequest request(Long tenantId, List<Long> permissions) {
        RoleRequest request = new RoleRequest();
        request.setTenantId(tenantId);
        request.setCode("editor");
        request.setName("Editor");
        request.setStatus("1");
        request.setPermissions(permissions);
        return request;
    }

    private static RoleBO role(Long id, Long tenantId) {
        RoleBO role = new RoleBO();
        role.setId(id);
        role.setTenantId(tenantId);
        role.setCode("editor");
        role.setName("Editor");
        role.setStatus(1);
        role.setDelFlag(0);
        return role;
    }

    private static TenantBO activeTenant(Long id) {
        TenantBO tenant = new TenantBO();
        tenant.setId(id);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        return tenant;
    }

    private static TenantBO inactiveTenant(Long id) {
        TenantBO tenant = activeTenant(id);
        tenant.setStatus(0);
        return tenant;
    }
}

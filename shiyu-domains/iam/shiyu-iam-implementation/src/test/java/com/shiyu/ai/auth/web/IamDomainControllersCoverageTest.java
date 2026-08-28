package com.shiyu.ai.auth.web;

import com.shiyu.ai.auth.request.*;
import com.shiyu.ai.auth.service.*;
import com.shiyu.ai.auth.vo.RoleVO;
import com.shiyu.ai.auth.vo.TenantVO;
import com.shiyu.ai.auth.vo.UserVO;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.knowledge.contract.KnowledgeTenantProvisioning;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IamDomainControllersCoverageTest {
    private final RoleService roles = mock(RoleService.class);
    private final UserService users = mock(UserService.class);
    private final AuthService auth = mock(AuthService.class);
    private final TenantService tenants = mock(TenantService.class);
    private final KnowledgeTenantProvisioning knowledge = mock(KnowledgeTenantProvisioning.class);

    @BeforeEach
    void actor() {
        UserContext context = new UserContext();
        context.setUserId(8L); context.setCurrentTenantId(7L); context.setHomeTenantId(7L);
        UserContextHolder.setContext(context);
    }

    @AfterEach
    void clear() { UserContextHolder.clearContext(); }

    @Test
    void mapsRoleControllerSuccessAndFailureBranches() {
        RoleController controller = new RoleController(roles);
        RolePageRequest page = new RolePageRequest();
        RoleRequest request = new RoleRequest();
        AssignUserRolesRequest assignments = new AssignUserRolesRequest();
        when(roles.getRoleList(any(), any(), any(), any())).thenReturn(null);
        when(roles.allRolesView(any(), any(), any(TenantId.class))).thenReturn(List.of());
        when(roles.detailView(any(), eq(1L), eq(new TenantId(7L)))).thenReturn(new RoleVO());
        when(roles.createRole(any(), same(request))).thenReturn(true);
        when(roles.updateRole(any(), eq(1L), same(request))).thenReturn(true);
        when(roles.replaceRoleMenus(any(), eq(1L), eq(new TenantId(7L)), eq(List.of(2L)))).thenReturn(true);
        when(roles.deleteRole(any(), eq(1L))).thenReturn(true);
        when(roles.removeUserRoles(any(), eq(1L), any(), any())).thenReturn(true);
        when(roles.assignUserRoles(any(), eq(1L), any(), any())).thenReturn(true);
        assignments.setTenantId(7L); assignments.setUserIds(List.of(8L));
        assertTrue(controller.getRoleList(page).isSuccess());
        assertTrue(controller.getAllRoles(null, 7L).isSuccess());
        assertTrue(controller.getRoleDetail(1L, 7L).isSuccess());
        assertTrue(controller.createRole(request).isSuccess());
        assertTrue(controller.updateRole(1L, request).isSuccess());
        assertTrue(controller.replaceRoleMenus(1L, 7L, List.of(2L)).isSuccess());
        assertTrue(controller.deleteRole(1L).isSuccess());
        assertTrue(controller.removeUserRoles(1L, assignments).isSuccess());
        assertTrue(controller.assignUserRoles(1L, assignments).isSuccess());

        assignments.setTenantId(null);
        assertTrue(controller.assignUserRoles(1L, assignments).isSuccess());
        verify(roles).assignUserRoles(any(), eq(1L), isNull(), eq(List.of(8L)));

        when(roles.detailView(any(), eq(2L), eq(new TenantId(7L)))).thenReturn(null);
        when(roles.createRole(any(), any())).thenReturn(false);
        when(roles.updateRole(any(), anyLong(), any())).thenReturn(false);
        when(roles.replaceRoleMenus(any(), anyLong(), any(TenantId.class), any())).thenReturn(false);
        when(roles.deleteRole(any(), anyLong())).thenReturn(false);
        when(roles.removeUserRoles(any(), anyLong(), any(), any())).thenReturn(false);
        when(roles.assignUserRoles(any(), anyLong(), any(), any())).thenReturn(false);
        assertFalse(controller.getRoleDetail(2L, 7L).isSuccess());
        assertFalse(controller.createRole(request).isSuccess());
        assertFalse(controller.updateRole(1L, request).isSuccess());
        assertFalse(controller.replaceRoleMenus(1L, 7L, List.of()).isSuccess());
        assertFalse(controller.deleteRole(1L).isSuccess());
        assertFalse(controller.removeUserRoles(1L, assignments).isSuccess());
        assertFalse(controller.assignUserRoles(1L, assignments).isSuccess());
    }

    @Test
    void mapsUserControllerOwnershipAndMutationBranches() {
        UserController controller = new UserController(users, auth);
        UserRequest request = new UserRequest();
        UserPageRequest page = new UserPageRequest();
        ResetPasswordRequest reset = new ResetPasswordRequest();
        ChangePasswordRequest change = new ChangePasswordRequest();
        UserVO user = new UserVO();
        when(users.detailView(any(), eq(8L))).thenReturn(user);
        when(auth.getUserTenants(any(), eq(8L))).thenReturn(List.of());
        when(users.getUserList(any(), any(), any(), any())).thenReturn(null);
        when(users.createUser(any(), same(request), any(), any())).thenReturn(Map.of());
        when(users.updateUser(any(), eq(8L), same(request), any(), any())).thenReturn(true);
        when(users.getTenantAssignments(any(), eq(8L))).thenReturn(List.of());
        when(users.replaceTenantAssignments(any(), eq(8L), any())).thenReturn(true);
        when(users.deleteUser(any(), eq(8L))).thenReturn(true);
        when(users.resetUserPassword(any(), eq(8L), any())).thenReturn("ok");
        when(users.changePassword(any(), eq(8L), any(), any())).thenReturn(true);
        assertTrue(controller.getUserInfo().isSuccess());
        assertTrue(controller.getUserList(page).isSuccess());
        assertTrue(controller.createUser(request).isSuccess());
        assertTrue(controller.updateUser(8L, request).isSuccess());
        assertTrue(controller.getTenantAssignments(8L).isSuccess());
        assertTrue(controller.replaceTenantAssignments(8L, List.of()).isSuccess());
        assertTrue(controller.deleteUser(8L).isSuccess());
        assertTrue(controller.resetPassword(8L, reset).isSuccess());
        assertTrue(controller.changePassword(8L, change).isSuccess());

        when(users.detailView(any(), eq(8L))).thenReturn(null);
        assertFalse(controller.getUserInfo().isSuccess());
        when(users.updateUser(any(), anyLong(), any(), any(), any())).thenReturn(false);
        when(users.replaceTenantAssignments(any(), anyLong(), any())).thenReturn(false);
        when(users.deleteUser(any(), anyLong())).thenReturn(false);
        when(users.resetUserPassword(any(), anyLong(), any())).thenReturn(null);
        when(users.changePassword(any(), anyLong(), any(), any())).thenReturn(false);
        assertFalse(controller.updateUser(8L, request).isSuccess());
        assertFalse(controller.replaceTenantAssignments(8L, List.of()).isSuccess());
        assertFalse(controller.deleteUser(8L).isSuccess());
        assertFalse(controller.resetPassword(8L, reset).isSuccess());
        assertFalse(controller.changePassword(8L, change).isSuccess());
    }

    @Test
    void mapsTenantProvisioningAndFailureBranches() {
        TenantController controller = new TenantController(tenants, auth, knowledge);
        TenantPageRequest page = new TenantPageRequest();
        TenantRequest request = new TenantRequest(); request.setCode("acme");
        TenantVO tenant = new TenantVO(); tenant.setId(11L); tenant.setCode("acme");
        when(tenants.allTenantsView(any())).thenReturn(List.of(tenant));
        when(tenants.getTenantPage(any(), any(), any(), any(), any(), any())).thenReturn(null);
        when(tenants.detailView(any(), eq(11L))).thenReturn(tenant);
        when(tenants.createTenant(any(), same(request))).thenReturn(true);
        when(tenants.updateTenant(any(), eq(11L), same(request))).thenReturn(true);
        when(tenants.deleteTenant(any(), eq(11L))).thenReturn(true);
        assertTrue(controller.getAllTenants().isSuccess());
        assertTrue(controller.getTenantPage(page).isSuccess());
        assertTrue(controller.getTenantById(11L).isSuccess());
        assertTrue(controller.createTenant(request).isSuccess());
        verify(knowledge).initializeTenantDefaults(new TenantId(11L));
        assertTrue(controller.updateTenant(11L, request).isSuccess());
        assertTrue(controller.deleteTenant(11L).isSuccess());

        when(tenants.detailView(any(), eq(12L))).thenReturn(null);
        when(tenants.createTenant(any(), any())).thenReturn(false);
        when(tenants.updateTenant(any(), anyLong(), any())).thenReturn(false);
        when(tenants.deleteTenant(any(), anyLong())).thenReturn(false);
        assertFalse(controller.getTenantById(12L).isSuccess());
        assertFalse(controller.createTenant(request).isSuccess());
        assertFalse(controller.updateTenant(11L, request).isSuccess());
        assertFalse(controller.deleteTenant(11L).isSuccess());

        TenantRequest withoutCode = new TenantRequest();
        when(tenants.createTenant(any(), same(withoutCode))).thenReturn(true);
        when(tenants.allTenantsView(any())).thenReturn(List.of(tenant));
        assertTrue(controller.createTenant(withoutCode).isSuccess());
        verify(knowledge, times(1)).initializeTenantDefaults(new TenantId(11L));
    }

    @Test
    void mapsTimezoneControllerMutationResult() {
        TimezoneService timezone = mock(TimezoneService.class);
        TimezoneController controller = new TimezoneController(timezone);
        SetTimezoneRequest request = new SetTimezoneRequest();
        ActorContext actor = new ActorContext(new TenantId(7L), new com.shiyu.ai.kernel.context.UserId(8L), false);
        try (var mocked = mockStatic(com.shiyu.ai.common.web.auth.ActorContextHttpAdapter.class)) {
            mocked.when(com.shiyu.ai.common.web.auth.ActorContextHttpAdapter::currentActor).thenReturn(actor);
            when(timezone.getTimezoneOptions()).thenReturn(List.of());
            when(timezone.getTimezone(actor)).thenReturn("Asia/Shanghai");
            when(timezone.setTimezone(actor, request)).thenReturn(true, false);
            assertTrue(controller.getTimezoneOptions().isSuccess());
            assertEquals("Asia/Shanghai", controller.getTimezone().getData());
            assertTrue(controller.setTimezone(request).isSuccess());
            assertFalse(controller.setTimezone(request).isSuccess());
        }
    }
}

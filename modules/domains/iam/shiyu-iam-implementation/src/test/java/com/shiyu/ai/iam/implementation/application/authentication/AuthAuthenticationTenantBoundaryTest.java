package com.shiyu.ai.iam.implementation.application.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.iam.implementation.application.identity.AuthTenantContextSupport;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.service.CaptchaService;
import com.shiyu.ai.iam.implementation.utils.SaTokenHelper;
import com.shiyu.ai.iam.implementation.vo.TenantContextVO;
import com.shiyu.ai.iam.implementation.vo.TenantInfoVO;
import com.shiyu.ai.kernel.context.TenantId;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Prevents a malformed historical role assignment from expanding login tenant scope. */
class AuthAuthenticationTenantBoundaryTest {
    @Test
    void loginRejectsDisabledOrDeletedCurrentTenantBeforeIssuingToken() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport contexts = mock(AuthTenantContextSupport.class);
        AuthAuthenticationUseCase authentication = new AuthAuthenticationUseCase(
                users, assignments, tenantRoles, mock(CaptchaService.class), contexts);
        UserBO user = new UserBO();
        user.setId(20L);
        user.setExtInfo("{\"currentTenantId\":10}");
        when(users.selectRolesByUserId(20L)).thenReturn(List.of());
        when(assignments.selectByUserId(20L)).thenReturn(List.of());
        when(contexts.parseExtInfo(user.getExtInfo())).thenReturn(Map.of("currentTenantId", 10L));
        when(contexts.resolveCurrentTenantId(user.getExtInfo(), List.of())).thenReturn(10L);
        TenantBO disabled = tenant(10L);
        disabled.setStatus(0);
        TenantBO unknownStatus = tenant(10L);
        unknownStatus.setStatus(null);
        TenantBO deleted = tenant(10L);
        deleted.setDelFlag(1);
        when(tenantRoles.selectTenantById(new TenantId(10L)))
                .thenReturn(null, unknownStatus, disabled, deleted);

        assertNull(authentication.completeLogin(user, null, null));
        assertNull(authentication.completeLogin(user, null, null));
        assertNull(authentication.completeLogin(user, null, null));
        assertNull(authentication.completeLogin(user, null, null));
        verify(users, never()).update(any());
    }

    @Test
    void registrationNeverGrantsAForeignOrInactiveDefaultRole() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        AuthAuthenticationUseCase authentication = new AuthAuthenticationUseCase(
                users, assignments, tenantRoles, mock(CaptchaService.class),
                mock(AuthTenantContextSupport.class));
        UserBO user = new UserBO();
        user.setId(20L);
        when(users.selectById(20L)).thenReturn(user);
        RoleBO role = role(30L, 99L, "user", 1);
        when(tenantRoles.selectEnabledRoleByCode(new TenantId(1L), "user"))
                .thenReturn(role);

        assertThrows(IllegalStateException.class,
                () -> authentication.assignDefaultTenantScopeRole(20L));
        role.setTenantId(1L);
        role.setStatus(0);
        assertThrows(IllegalStateException.class,
                () -> authentication.assignDefaultTenantScopeRole(20L));
        role.setStatus(1);
        role.setDelFlag(1);
        assertThrows(IllegalStateException.class,
                () -> authentication.assignDefaultTenantScopeRole(20L));
        role.setDelFlag(0);
        role.setCode("tenant_super");
        assertThrows(IllegalStateException.class,
                () -> authentication.assignDefaultTenantScopeRole(20L));
        verify(assignments, never()).insert(any());
    }

    @Test
    void neverRestoresForeignDelegatedRoleDuringLogin() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport contexts = mock(AuthTenantContextSupport.class);
        AuthAuthenticationUseCase authentication = new AuthAuthenticationUseCase(
                users, assignments, tenantRoles, mock(CaptchaService.class), contexts);
        UserBO user = new UserBO();
        user.setId(20L);
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":20,"
                + "\"switchMode\":\"PARENT_SUPER_ADMIN\"}");
        when(users.selectRolesByUserId(20L)).thenReturn(List.of());
        when(assignments.selectByUserId(20L)).thenReturn(List.of());
        Map<String, Object> saved = Map.of(
                "homeTenantId", 10L, "currentTenantId", 20L,
                "switchMode", "PARENT_SUPER_ADMIN");
        when(contexts.parseExtInfo(user.getExtInfo())).thenReturn(saved);
        when(contexts.resolveCurrentTenantId(user.getExtInfo(), List.of())).thenReturn(20L);
        when(contexts.isDelegatedTenantContext(saved, List.of(), 20L)).thenReturn(true);
        when(contexts.findTenantSuperRole(20L))
                .thenReturn(role(40L, 99L, "tenant_super", 1));

        assertNull(authentication.completeLogin(user, null, null));
        verify(contexts, never()).buildExtInfo(any(), any(), any(), any(), any());
        verify(users, never()).update(user);
    }

    @Test
    void loginResponseOmitsForeignAndDisabledAssignedRoles() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport contexts = mock(AuthTenantContextSupport.class);
        AuthAuthenticationUseCase authentication = new AuthAuthenticationUseCase(
                users, assignments, tenantRoles, mock(CaptchaService.class), contexts);
        UserBO user = new UserBO();
        user.setId(20L);
        user.setUsername("member");
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":10}");
        RoleBO valid = role(30L, 10L, "member", 1);
        RoleBO foreign = role(31L, 99L, "tenant_super", 1);
        RoleBO disabled = role(32L, 10L, "manager", 0);
        List<UserScopeRoleBO> grants = List.of(
                grant(20L, 10L, 30L), grant(20L, 10L, 31L), grant(20L, 10L, 32L));
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(valid, foreign, disabled));
        when(assignments.selectByUserId(20L)).thenReturn(grants);
        when(contexts.parseExtInfo(user.getExtInfo()))
                .thenReturn(Map.of("homeTenantId", 10L, "currentTenantId", 10L));
        when(contexts.resolveCurrentTenantId(user.getExtInfo(), grants)).thenReturn(10L);
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant(10L));
        when(contexts.numberValue(10L)).thenReturn(10L);
        for (UserScopeRoleBO grant : grants) {
            when(contexts.isActiveAssignment(grant)).thenReturn(true);
        }
        when(contexts.buildExtInfo(any(), nullable(RoleBO.class), any(), any(LocalDateTime.class), any()))
                .thenReturn(new HashMap<>());
        when(contexts.buildTenantList(grants)).thenReturn(List.of());
        when(contexts.buildSubTenantList(grants, 10L)).thenReturn(List.of());
        when(users.update(user)).thenReturn(true);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.loginWithKickout(20L)).thenReturn("token");
        when(helper.getTokenTimeout()).thenReturn(7200L);

        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertEquals(List.of("member"),
                    authentication.completeLogin(user, null, null).getRoles());
        }
    }

    @Test
    void homeSuperRoleDoesNotExpandUnrelatedCurrentTenant() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport contexts = mock(AuthTenantContextSupport.class);
        AuthAuthenticationUseCase authentication = new AuthAuthenticationUseCase(
                users, assignments, tenantRoles, mock(CaptchaService.class), contexts);
        UserBO user = new UserBO();
        user.setId(20L);
        user.setUsername("member");
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":20}");
        RoleBO homeSuper = new RoleBO();
        homeSuper.setId(7L);
        homeSuper.setTenantId(10L);
        homeSuper.setCode("tenant_super");
        homeSuper.setStatus(1);
        homeSuper.setDelFlag(0);
        RoleBO currentRole = new RoleBO();
        currentRole.setId(8L);
        currentRole.setTenantId(20L);
        currentRole.setCode("member");
        currentRole.setStatus(1);
        currentRole.setDelFlag(0);
        UserScopeRoleBO homeGrant = grant(20L, 10L, 7L);
        UserScopeRoleBO currentGrant = grant(20L, 20L, 8L);
        List<UserScopeRoleBO> grants = List.of(homeGrant, currentGrant);
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(homeSuper, currentRole));
        when(assignments.selectByUserId(20L)).thenReturn(grants);
        when(contexts.parseExtInfo(user.getExtInfo()))
                .thenReturn(Map.of("homeTenantId", 10L, "currentTenantId", 20L));
        when(contexts.resolveCurrentTenantId(user.getExtInfo(), grants)).thenReturn(20L);
        when(tenantRoles.selectTenantById(new TenantId(20L))).thenReturn(tenant(20L));
        when(contexts.numberValue(10L)).thenReturn(10L);
        when(contexts.isActiveAssignment(homeGrant)).thenReturn(true);
        when(contexts.isActiveAssignment(currentGrant)).thenReturn(true);
        when(contexts.resolveCurrentRoleForTenant(null, List.of(currentRole), grants, 20L))
                .thenReturn(currentRole);
        when(contexts.buildExtInfo(any(), any(), any(), any(LocalDateTime.class), any()))
                .thenReturn(new HashMap<>(Map.of("switchMode", "NORMAL")));
        when(contexts.buildTenantList(grants)).thenReturn(List.<TenantInfoVO>of());
        when(contexts.buildSubTenantList(grants, 20L)).thenReturn(List.<TenantContextVO>of());
        when(tenantRoles.selectRoleById(7L)).thenReturn(homeSuper);
        when(contexts.isTenantSuperRole(homeSuper)).thenReturn(true);
        when(users.update(user)).thenReturn(true);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.loginWithKickout(20L)).thenReturn("token");
        when(helper.getTokenTimeout()).thenReturn(7200L);

        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertNotNull(authentication.completeLogin(user, null, null));
        }
        verify(contexts, never()).buildScopedTenantList(any(), any());
    }

    @Test
    void staleDelegatedModeDoesNotRestoreTenantSuperRole() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport contexts = mock(AuthTenantContextSupport.class);
        AuthAuthenticationUseCase authentication = new AuthAuthenticationUseCase(
                users, assignments, tenantRoles, mock(CaptchaService.class), contexts);
        UserBO user = new UserBO();
        user.setId(20L);
        user.setUsername("member");
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":20,\"switchMode\":\"PARENT_SUPER_ADMIN\"}");
        UserScopeRoleBO currentGrant = grant(20L, 20L, 8L);
        List<UserScopeRoleBO> grants = List.of(currentGrant);
        Map<String, Object> saved = Map.of(
                "homeTenantId", 10L,
                "currentTenantId", 20L,
                "switchMode", "PARENT_SUPER_ADMIN");
        when(users.selectRolesByUserId(20L)).thenReturn(List.of());
        when(assignments.selectByUserId(20L)).thenReturn(grants);
        when(contexts.parseExtInfo(user.getExtInfo())).thenReturn(saved);
        when(contexts.resolveCurrentTenantId(user.getExtInfo(), grants)).thenReturn(20L);
        when(tenantRoles.selectTenantById(new TenantId(20L))).thenReturn(tenant(20L));
        when(contexts.numberValue(10L)).thenReturn(10L);
        when(contexts.isActiveAssignment(currentGrant)).thenReturn(true);
        when(contexts.findTenantSuperRole(20L)).thenReturn(new RoleBO());
        when(contexts.buildExtInfo(any(), any(), any(), any(LocalDateTime.class), any()))
                .thenReturn(new HashMap<>(Map.of(
                        "switchMode", "PARENT_SUPER_ADMIN",
                        "switchFromTenantId", 10L,
                        "currentRole", Map.of("roleId", 90L, "roleKey", "tenant_super"))));
        when(contexts.buildTenantList(grants)).thenReturn(List.<TenantInfoVO>of());
        when(contexts.buildSubTenantList(grants, 20L)).thenReturn(List.<TenantContextVO>of());
        when(users.update(user)).thenReturn(true);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.loginWithKickout(20L)).thenReturn("token");
        when(helper.getTokenTimeout()).thenReturn(7200L);

        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertEquals("NORMAL", authentication.completeLogin(user, null, null).getSwitchMode());
        }
        Map<String, Object> updated = JSONUtils.parseMap(user.getExtInfo());
        assertEquals("NORMAL", updated.get("switchMode"));
        assertFalse(updated.containsKey("switchFromTenantId"));
        assertFalse(updated.containsKey("currentRole"));
        verify(contexts, never()).findTenantSuperRole(20L);
    }

    @Test
    void foreignTenantSuperRoleDoesNotExpandLoginTenantList() {
        UserRepository users = mock(UserRepository.class);
        UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
        TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport contexts = mock(AuthTenantContextSupport.class);
        AuthAuthenticationUseCase authentication = new AuthAuthenticationUseCase(
                users, assignments, tenantRoles, mock(CaptchaService.class), contexts);
        UserBO user = new UserBO();
        user.setId(20L);
        user.setUsername("member");
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":20}");
        RoleBO foreignSuper = new RoleBO();
        foreignSuper.setId(7L);
        foreignSuper.setTenantId(99L);
        foreignSuper.setCode("tenant_super");
        foreignSuper.setStatus(1);
        foreignSuper.setDelFlag(0);
        RoleBO currentRole = new RoleBO();
        currentRole.setId(8L);
        currentRole.setTenantId(20L);
        currentRole.setCode("member");
        currentRole.setStatus(1);
        currentRole.setDelFlag(0);
        UserScopeRoleBO homeGrant = grant(20L, 10L, 7L);
        UserScopeRoleBO currentGrant = grant(20L, 20L, 8L);
        List<UserScopeRoleBO> grants = List.of(homeGrant, currentGrant);
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(foreignSuper, currentRole));
        when(assignments.selectByUserId(20L)).thenReturn(grants);
        when(contexts.parseExtInfo(user.getExtInfo()))
                .thenReturn(Map.of("homeTenantId", 10L, "currentTenantId", 20L));
        when(contexts.resolveCurrentTenantId(user.getExtInfo(), grants)).thenReturn(20L);
        when(tenantRoles.selectTenantById(new TenantId(20L))).thenReturn(tenant(20L));
        when(contexts.numberValue(10L)).thenReturn(10L);
        when(contexts.isActiveAssignment(homeGrant)).thenReturn(true);
        when(contexts.isActiveAssignment(currentGrant)).thenReturn(true);
        when(contexts.resolveCurrentRoleForTenant(null, List.of(currentRole), grants, 20L))
                .thenReturn(currentRole);
        when(contexts.buildExtInfo(any(), any(), any(), any(LocalDateTime.class), any()))
                .thenReturn(new HashMap<>(Map.of("switchMode", "NORMAL")));
        when(contexts.buildTenantList(grants)).thenReturn(List.<TenantInfoVO>of());
        when(contexts.buildSubTenantList(grants, 20L)).thenReturn(List.<TenantContextVO>of());
        when(tenantRoles.selectRoleById(7L)).thenReturn(foreignSuper);
        when(contexts.isTenantSuperRole(foreignSuper)).thenReturn(true);
        when(users.update(user)).thenReturn(true);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.loginWithKickout(20L)).thenReturn("token");
        when(helper.getTokenTimeout()).thenReturn(7200L);

        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertNotNull(authentication.completeLogin(user, null, null));
        }
        verify(contexts, never()).buildScopedTenantList(any(), any());
    }

    private static UserScopeRoleBO grant(Long userId, Long tenantId, Long roleId) {
        UserScopeRoleBO value = new UserScopeRoleBO();
        value.setUserId(userId);
        value.setTenantId(tenantId);
        value.setRoleId(roleId);
        value.setStatus(1);
        value.setDelFlag(0);
        return value;
    }

    private static RoleBO role(Long id, Long tenantId, String code, Integer status) {
        RoleBO value = new RoleBO();
        value.setId(id);
        value.setTenantId(tenantId);
        value.setCode(code);
        value.setStatus(status);
        value.setDelFlag(0);
        return value;
    }

    private static TenantBO tenant(Long id) {
        TenantBO value = new TenantBO();
        value.setId(id);
        value.setName("tenant-" + id);
        value.setStatus(1);
        value.setDelFlag(0);
        return value;
    }
}

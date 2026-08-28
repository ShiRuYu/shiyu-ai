package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.auth.port.repository.*;
import com.shiyu.ai.auth.service.CaptchaService;
import com.shiyu.ai.auth.service.MenuService;
import com.shiyu.ai.auth.vo.TenantInfoVO;
import com.shiyu.ai.auth.vo.LoginResponseVO;
import com.shiyu.ai.auth.utils.SaTokenHelper;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
    private final AuthRepository auth = mock(AuthRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
    private final TenantRoleRepository tenantRoles = mock(TenantRoleRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final MenuService menus = mock(MenuService.class);
    private final CaptchaService captcha = mock(CaptchaService.class);
    private final AuthServiceImpl service = new AuthServiceImpl(auth, users, assignments, tenantRoles, tenants, menus, captcha);

    @Test
    void selectsPermissionCodesForUsernameAndActorModes() {
        ActorContext normal = new ActorContext(new TenantId(10), new UserId(20), new RoleId(3), "editor", new TenantId(10), null, false);
        when(auth.selectCodesByUsername("alice", new TenantId(10L))).thenReturn(List.of("read"));
        assertEquals(List.of("read"), service.getAuthCodes(normal, "alice"));
        when(auth.selectCodesByUsername("empty", new TenantId(10L))).thenReturn(null);
        assertTrue(service.getAuthCodes(normal, "empty").isEmpty());
        when(auth.selectCodesByUsername("broken", new TenantId(10L))).thenThrow(new IllegalStateException("db"));
        assertTrue(service.getAuthCodes(normal, "broken").isEmpty());
        assertTrue(service.getAuthCodes(null, "alice").isEmpty());

        when(auth.selectCodesByUserIdAndRoleCode(new UserId(20L), new TenantId(10L), "editor")).thenReturn(List.of("write"));
        assertEquals(List.of("write"), service.getAuthCodesByUserId(normal, new UserId(20L)));
        ActorContext delegated = new ActorContext(new TenantId(11), new UserId(20), new RoleId(3), "tenant_super", new TenantId(10), "PARENT_SUPER_ADMIN", false);
        when(auth.selectCodesByRoleCodeAndTenant("tenant_super", new TenantId(11L))).thenReturn(List.of("admin"));
        assertEquals(List.of("admin"), service.getAuthCodesByUserId(delegated, new UserId(20L)));
        ActorContext noRole = new ActorContext(new TenantId(10), new UserId(20), false);
        when(auth.selectCodesByUserId(new UserId(20L), new TenantId(10L))).thenReturn(List.of("read"));
        assertEquals(List.of("read"), service.getAuthCodesByUserId(noRole, new UserId(20L)));
        assertTrue(service.getAuthCodesByUserId(null, new UserId(20L)).isEmpty());
        assertTrue(service.getAuthCodesByUserId(noRole, new UserId(21L)).isEmpty());
    }

    @Test
    void rejectsUnknownDisabledAndWrongPasswordLogins() {
        when(users.selectActiveUserByUsername("missing")).thenReturn(null);
        assertNull(service.login("missing", "secret"));
        UserBO disabled = user(1L, "disabled", "secret", 0);
        when(users.selectActiveUserByUsername("disabled")).thenReturn(disabled);
        assertNull(service.login("disabled", "secret"));
        UserBO wrong = user(2L, "wrong", "different", 1);
        when(users.selectActiveUserByUsername("wrong")).thenReturn(wrong);
        assertNull(service.login("wrong", "secret"));
    }

    @Test
    void handlesAuthenticationAndRoleResolutionFailurePaths() {
        when(users.selectActiveUserByUsername("broken")).thenThrow(new IllegalStateException("database down"));
        assertNull(service.login("broken", "secret"));

        UserBO withoutId = user(null, "no-id", "secret", 1);
        when(users.selectActiveUserByUsername("no-id")).thenReturn(withoutId);
        assertNull(service.login("no-id", "secret"));

        UserBO noRoles = user(30L, "no-role", "secret", 1);
        noRoles.setExtInfo("{\"currentTenantId\":\"not-a-number\"}");
        when(users.selectActiveUserByUsername("no-role")).thenReturn(noRoles);
        when(users.selectRolesByUserId(30L)).thenReturn(List.of());
        when(assignments.selectByUserId(30L)).thenReturn(List.of());
        assertNull(service.login("no-role", "secret"));
        verify(users, never()).update(noRoles);
    }

    @Test
    void coversPermissionFallbackAndRoleSwitchFailure() {
        ActorContext actor = new ActorContext(new TenantId(10), new UserId(20), false);
        when(auth.selectCodesByUserIdAndRoleCode(new UserId(20L), new TenantId(10L), "editor")).thenReturn(null);
        assertTrue(service.getAuthCodesByUserId(actor, new UserId(20L)).isEmpty());
        when(auth.selectCodesByUserIdAndRoleCode(new UserId(20L), new TenantId(10L), "editor"))
                .thenThrow(new IllegalStateException("permission lookup failed"));
        assertTrue(service.getAuthCodesByUserId(actor, new UserId(20L)).isEmpty());

        UserBO user = user(20L, "alice", "secret", 1);
        user.setExtInfo("{\"currentTenantId\":10}");
        RoleBO role = role(3L, 10L, "editor", "Editor");
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 3L)));
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(role));
        doThrow(new IllegalStateException("write failed")).when(users).update(user);
        assertFalse(service.switchCurrentRole(20L, 3L));
    }

    @Test
    void switchesToDelegatedChildTenantAndBuildsScopedTenantList() {
        UserBO user = user(20L, "parent", "secret", 1);
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":10}");
        RoleBO parentSuper = role(7L, 10L, "tenant_super", "Parent Admin");
        RoleBO childSuper = role(8L, 11L, "tenant_super", "Child Admin");
        TenantBO parent = tenant(10L, "parent", "Parent");
        TenantBO child = tenant(11L, "child", "Child");
        child.setParentId(10L);
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 7L)));
        when(tenantRoles.selectTenantById(new TenantId(11L))).thenReturn(child);
        when(tenantRoles.selectRoleById(7L)).thenReturn(parentSuper);
        when(tenantRoles.selectTenantSuperRole(new TenantId(11L))).thenReturn(childSuper);
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(11L));
        when(tenants.selectDescendantIds(new TenantId(11L))).thenReturn(List.of(11L, 12L));
        when(tenants.selectAll()).thenReturn(List.of(parent, child));
        when(users.update(user)).thenReturn(true);

        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::clearUserContextSession).thenAnswer(invocation -> null);
            assertTrue(service.switchCurrentTenant(20L, new TenantId(11L)));
        }
        assertTrue(user.getExtInfo().contains("PARENT_SUPER_ADMIN"));
        assertTrue(user.getExtInfo().contains("switchFromTenantId"));

        ActorContext actor = new ActorContext(new TenantId(11L), new UserId(20L), false);
        assertEquals(2, service.getUserTenants(actor, 20L).size());
    }

    @Test
    void completesSuccessfulLoginWithTenantRoleAndSignedToken() {
        UserBO user = user(20L, "alice", "secret", 1);
        user.setNickName("Alice");
        user.setExtInfo("{\"currentTenantId\":10,\"homeTenantId\":10}");
        RoleBO editor = role(3L, 10L, "editor", "Editor");
        UserScopeRoleBO assignment = assignment(20L, 10L, 3L);
        TenantBO tenant = tenant(10L, "TENANT", "Tenant");
        when(users.selectActiveUserByUsername("alice")).thenReturn(user);
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(editor));
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment));
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant);
        when(tenantRoles.selectRoleById(3L)).thenReturn(editor);
        when(users.update(user)).thenReturn(true);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.loginWithKickout(20L)).thenReturn("access-token");
        when(helper.getTokenTimeout()).thenReturn(3600L);
        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            LoginResponseVO response = service.login("alice", "secret", 3L);
            assertNotNull(response);
            assertEquals("access-token", response.getAccessToken());
            assertEquals(10L, response.getCurrentTenantId());
            assertEquals(List.of("editor"), response.getRoles());
            verify(users).update(user);
        }
    }

    @Test
    void switchesRoleWithinCurrentTenant() {
        UserBO user = user(20L, "alice", "secret", 1);
        user.setExtInfo("{\"currentTenantId\":10}");
        RoleBO role = role(3L, 10L, "editor", "Editor");
        UserScopeRoleBO assignment = assignment(20L, 10L, 3L);
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment));
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(role));
        when(users.update(user)).thenReturn(true);

        assertTrue(service.switchCurrentRole(20L, 3L));
        verify(users).update(user);
        verify(menus).evictRouteMenuCache(20L);
        assertTrue(user.getExtInfo().contains("editor"));
    }

    @Test
    void rejectsRoleSwitchWhenTenantContextPersistenceReturnsFalse() {
        UserBO user = user(20L, "alice", "secret", 1);
        user.setExtInfo("{\"currentTenantId\":10}");
        RoleBO role = role(3L, 10L, "editor", "Editor");
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 3L)));
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(role));
        when(users.update(user)).thenReturn(false);

        assertFalse(service.switchCurrentRole(20L, 3L));
        verify(menus, never()).evictRouteMenuCache(anyLong());
    }

    @Test
    void rejectsRoleAndTenantSwitchesThatViolateOwnershipOrStatus() {
        assertFalse(service.switchCurrentRole(null, 3L));
        assertFalse(service.switchCurrentRole(20L, null));
        when(users.selectById(20L)).thenReturn(null);
        assertFalse(service.switchCurrentRole(20L, 3L));

        UserBO user = user(20L, "alice", "secret", 1);
        user.setExtInfo("{\"currentTenantId\":10}");
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 11L, 3L)));
        assertFalse(service.switchCurrentRole(20L, 3L));
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 3L)));
        when(users.selectRolesByUserId(20L)).thenReturn(List.of());
        assertFalse(service.switchCurrentRole(20L, 3L));

        assertFalse(service.switchCurrentTenant(20L, null));
        when(users.selectById(20L)).thenReturn(null);
        assertFalse(service.switchCurrentTenant(20L, new TenantId(10L)));
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of());
        assertFalse(service.switchCurrentTenant(20L, new TenantId(10L)));
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 3L)));
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant(10L, "TENANT", "Tenant"));
        RoleBO disabled = role(3L, 10L, "user", "User");
        disabled.setStatus(0);
        when(tenantRoles.selectRoleById(3L)).thenReturn(disabled);
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L));
        assertFalse(service.switchCurrentTenant(20L, new TenantId(10L)));
    }

    @Test
    void switchesTenantWithAssignedActiveRole() {
        UserBO user = user(20L, "alice", "secret", 1);
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":10}");
        RoleBO role = role(3L, 10L, "user", "User");
        TenantBO tenant = tenant(10L, "TENANT", "Tenant");
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 3L)));
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant);
        when(tenantRoles.selectRoleById(3L)).thenReturn(role);
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L));
        when(users.update(user)).thenReturn(true);

        assertTrue(service.switchCurrentTenant(20L, new TenantId(10L)));
        verify(users).update(user);
        verify(menus).evictRouteMenuCache(20L);
        assertTrue(user.getExtInfo().contains("NORMAL"));
    }

    @Test
    void rejectsTenantSwitchWhenTenantContextPersistenceReturnsFalse() {
        UserBO user = user(20L, "alice", "secret", 1);
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":10}");
        RoleBO role = role(3L, 10L, "user", "User");
        TenantBO tenant = tenant(10L, "TENANT", "Tenant");
        when(users.selectById(20L)).thenReturn(user);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 3L)));
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant);
        when(tenantRoles.selectRoleById(3L)).thenReturn(role);
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L));
        when(users.update(user)).thenReturn(false);

        assertFalse(service.switchCurrentTenant(20L, new TenantId(10L)));
        verify(menus, never()).evictRouteMenuCache(anyLong());
    }

    @Test
    void listsTenantAssignmentsAndRejectsInvalidPasswordRecovery() {
        ActorContext actor = new ActorContext(new TenantId(10), new UserId(20), false);
        UserBO user = user(20L, "alice", "secret", 1);
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 3L)));
        when(users.selectById(20L)).thenReturn(user);
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant(10L, "TENANT", "Tenant"));
        assertEquals(1, service.getUserTenants(actor, 20L).size());
        assertThrows(IllegalArgumentException.class, () -> service.forgetPassword("a@b.test", "new", "bad", "captcha"));
        when(captcha.validateCaptcha("captcha", "ok")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.forgetPassword("a@b.test", "new", "ok", "captcha"));
    }

    @Test
    void returnsPlainTenantAssignmentsWhenUserOrScopeDataIsMissing() {
        ActorContext actor = new ActorContext(new TenantId(10), new UserId(20), false);
        when(assignments.selectByUserId(99L)).thenReturn(null);
        when(users.selectById(99L)).thenReturn(null);
        assertTrue(service.getUserTenants(actor, 99L).isEmpty());

        UserBO user = user(100L, "plain", "secret", 1);
        user.setExtInfo("{}");
        when(assignments.selectByUserId(100L)).thenReturn(null);
        when(users.selectById(100L)).thenReturn(user);
        assertTrue(service.getUserTenants(actor, 100L).isEmpty());
    }

    @Test
    void refreshesAndLogsOutOnlyValidTokens() {
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.getUserIdByToken("old")).thenReturn(20L);
        when(helper.refreshToken(20L)).thenReturn("new");
        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertEquals("new", service.refreshToken("old"));
            verify(helper).refreshToken(20L);
            when(helper.getUserIdByToken("bad")).thenReturn(null);
            assertNull(service.refreshToken("bad"));
            service.logout("old");
            verify(helper).logout(20L);
            service.logout("bad");
            verify(helper, times(1)).logout(20L);
            when(helper.getUserIdByToken("throws")).thenThrow(new IllegalStateException("expired"));
            assertNull(service.refreshToken("throws"));
        }
    }

    @Test
    void registersAndCodeLogsInWithDefaultScopeRole() {
        RoleBO defaultRole = role(3L, 1L, "user", "User");
        UserBO registered = user(21L, "new-user", "secret", 1);
        when(users.selectByUsername("new-user")).thenReturn(null);
        doAnswer(invocation -> {
            UserBO value = invocation.getArgument(0);
            value.setId(21L);
            return value;
        }).when(users).insert(any(UserBO.class));
        when(users.selectById(21L)).thenReturn(registered);
        when(tenantRoles.selectEnabledRoleByCode(new TenantId(1L), "user")).thenReturn(defaultRole);
        when(users.update(any(UserBO.class))).thenReturn(true);
        when(users.selectActiveUserByUsername("new-user")).thenReturn(registered);
        when(users.selectRolesByUserId(21L)).thenReturn(List.of(defaultRole));
        when(assignments.selectByUserId(21L)).thenReturn(List.of(assignment(21L, 1L, 3L)));
        when(tenantRoles.selectTenantById(new TenantId(1L))).thenReturn(tenant(1L, "default", "Default"));
        when(tenantRoles.selectRoleById(3L)).thenReturn(defaultRole);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.loginWithKickout(21L)).thenReturn("registered-token");
        when(helper.getTokenTimeout()).thenReturn(7200L);
        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            LoginResponseVO response = service.register("new-user", "secret", "new@test.local");
            assertNotNull(response);
            assertEquals("registered-token", response.getAccessToken());
            verify(assignments).insert(any(UserScopeRoleBO.class));

            when(captcha.validateCaptcha("key", "code")).thenReturn(false);
            assertThrows(IllegalArgumentException.class, () -> service.codeLogin("phone", "code", "key"));
            when(captcha.validateCaptcha("key", "code")).thenReturn(true);
            when(users.selectByUsername("phone")).thenReturn(null);
            UserBO phoneUser = user(22L, "phone", "unused", 1);
            doAnswer(invocation -> {
                UserBO value = invocation.getArgument(0);
                value.setId(22L);
                return value;
            }).when(users).insert(argThat(value -> "phone".equals(value.getUsername())));
            when(users.selectById(22L)).thenReturn(phoneUser);
            when(tenantRoles.selectEnabledRoleByCode(new TenantId(1L), "user")).thenReturn(defaultRole);
            when(users.selectRolesByUserId(22L)).thenReturn(List.of(defaultRole));
            when(assignments.selectByUserId(22L)).thenReturn(List.of(assignment(22L, 1L, 3L)));
            LoginResponseVO codeResponse = service.codeLogin("phone", "code", "key");
            assertNotNull(codeResponse);
            assertEquals(22L, codeResponse.getId());
        }
    }

    @Test
    void buildsDelegatedTenantTreeAndTenantPaths() {
        UserBO user = user(20L, "parent-admin", "secret", 1);
        user.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":11,\"switchMode\":\"PARENT_SUPER_ADMIN\"}");
        RoleBO homeSuper = role(7L, 10L, "tenant_super", "Tenant Admin");
        RoleBO childSuper = role(8L, 11L, "tenant_super", "Tenant Admin");
        when(users.selectActiveUserByUsername("parent-admin")).thenReturn(user);
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(homeSuper));
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment(20L, 10L, 7L)));
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(11L));
        when(tenants.selectDescendantIds(new TenantId(11L))).thenReturn(List.of(11L));
        when(tenantRoles.selectRoleById(7L)).thenReturn(homeSuper);
        when(tenantRoles.selectTenantSuperRole(new TenantId(11L))).thenReturn(childSuper);
        when(tenantRoles.selectTenantById(new TenantId(11L))).thenReturn(tenant(11L, "child", "Child"));
        when(users.update(user)).thenReturn(true);
        TenantBO parent = tenant(10L, "parent", "Parent");
        TenantBO child = tenant(11L, "child", "Child");
        child.setParentId(10L);
        TenantBO disabled = tenant(12L, "disabled", "Disabled");
        disabled.setStatus(0);
        when(tenants.selectAll()).thenReturn(Arrays.asList(parent, child, disabled));
        SaTokenHelper helper = mock(SaTokenHelper.class);
        when(helper.loginWithKickout(20L)).thenReturn("delegated-token");
        when(helper.getTokenTimeout()).thenReturn(7200L);
        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            LoginResponseVO response = service.login("parent-admin", "secret");
            assertNotNull(response);
            assertEquals(11L, response.getCurrentTenantId());
            assertEquals(2, response.getTenants().size());
            assertTrue(response.getTenants().stream().anyMatch(item -> "Parent / Child".equals(item.getPathName())));
            assertEquals(List.of("tenant_super"), response.getRoles());
        }
    }

    @Test
    void rejectsLoginWhenLastLoginPersistenceReturnsFalse() {
        UserBO user = user(20L, "alice", "secret", 1);
        user.setExtInfo("{\"currentTenantId\":10,\"homeTenantId\":10}");
        RoleBO editor = role(3L, 10L, "editor", "Editor");
        UserScopeRoleBO assignment = assignment(20L, 10L, 3L);
        when(users.selectActiveUserByUsername("alice")).thenReturn(user);
        when(users.selectRolesByUserId(20L)).thenReturn(List.of(editor));
        when(assignments.selectByUserId(20L)).thenReturn(List.of(assignment));
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant(10L, "TENANT", "Tenant"));
        when(tenantRoles.selectRoleById(3L)).thenReturn(editor);
        when(users.update(user)).thenReturn(false);
        SaTokenHelper helper = mock(SaTokenHelper.class);
        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertNull(service.login("alice", "secret", 3L));
            verify(helper, never()).loginWithKickout(anyLong());
        }
    }

    @Test
    void resolvesRoleSelectionBoundaries() throws Exception {
        RoleBO role = role(3L, 10L, "editor", "Editor");
        UserScopeRoleBO assignment = assignment(20L, 10L, 3L);
        assertEquals(role, invoke(service, "resolveCurrentRoleForTenant", null,
                List.of(role), List.of(assignment), 10L));
        assertNull(invoke(service, "resolveCurrentRoleForTenant", null, List.of(), List.of(), 10L));
        assertNull(invoke(service, "resolveCurrentRoleForTenant", null, List.of(role), null, 10L));

        Map<String, Object> ext = invoke(service, "buildExtInfo", null, null, null,
                LocalDateTime.now(), "127.0.0.1");
        assertEquals("127.0.0.1", ext.get("lastLoginIp"));
        role.setStatus(null);
        assertFalse((Boolean) invoke(service, "isTenantSuperRole", role));
    }

    @Test
    void coversTenantResolutionAndCollectionBoundaryBranches() throws Exception {
        assertTrue(((Map<?, ?>) invoke(service, "parseExtInfo", (Object) null)).isEmpty());
        assertTrue(((Map<?, ?>) invoke(service, "parseExtInfo", "")).isEmpty());
        assertEquals(10, ((Map<?, ?>) invoke(service, "parseExtInfo", "{\"currentTenantId\":10}")).get("currentTenantId"));

        UserScopeRoleBO active = assignment(20L, 10L, 3L);
        UserScopeRoleBO inactive = assignment(20L, 10L, 3L);
        inactive.setStatus(0);
        UserScopeRoleBO deleted = assignment(20L, 10L, 3L);
        deleted.setDelFlag(1);
        UserScopeRoleBO noTenant = assignment(20L, null, 3L);
        UserScopeRoleBO noRole = assignment(20L, 10L, null);
        assertTrue((Boolean) invoke(service, "isActiveAssignment", active));
        assertFalse((Boolean) invoke(service, "isActiveAssignment", (Object) null));
        assertFalse((Boolean) invoke(service, "isActiveAssignment", inactive));
        assertFalse((Boolean) invoke(service, "isActiveAssignment", deleted));
        assertFalse((Boolean) invoke(service, "isActiveAssignment", noTenant));
        assertFalse((Boolean) invoke(service, "isActiveAssignment", noRole));

        TenantBO validTenant = tenant(10L, "t10", "Tenant 10");
        TenantBO disabledTenant = tenant(11L, "t11", "Tenant 11");
        disabledTenant.setStatus(0);
        TenantBO deletedTenant = tenant(12L, "t12", "Tenant 12");
        deletedTenant.setDelFlag(1);
        assertTrue((Boolean) invoke(service, "isActiveTenant", validTenant));
        assertFalse((Boolean) invoke(service, "isActiveTenant", (Object) null));
        assertFalse((Boolean) invoke(service, "isActiveTenant", disabledTenant));
        assertFalse((Boolean) invoke(service, "isActiveTenant", deletedTenant));

        assertEquals(10L, (Long) invoke(service, "resolveCurrentTenantId", "{\"currentTenantId\":10}", List.of(active)));
        assertEquals(10L, (Long) invoke(service, "resolveCurrentTenantId", "{\"currentTenantId\":99}", List.of(active)));
        assertNull(invoke(service, "resolveCurrentTenantId", "{}", List.of(inactive)));
        assertNull(invoke(service, "resolveCurrentTenantId", null, null));
        assertFalse((Boolean) invoke(service, "hasTenantAssignment", null, 10L));
        assertFalse((Boolean) invoke(service, "hasTenantAssignment", List.of(inactive), 10L));
        assertTrue((Boolean) invoke(service, "hasTenantAssignment", List.of(active), 10L));

        Map<String, Object> normal = Map.of("switchMode", "NORMAL", "homeTenantId", 10L);
        assertFalse((Boolean) invoke(service, "isDelegatedTenantContext", normal, List.of(active), 11L));
        Map<String, Object> delegated = Map.of("switchMode", "PARENT_SUPER_ADMIN", "homeTenantId", 10L);
        RoleBO parentSuper = role(7L, 10L, "tenant_super", "Parent");
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(11L));
        when(tenantRoles.selectRoleById(7L)).thenReturn(parentSuper);
        assertTrue((Boolean) invoke(service, "isDelegatedTenantContext", delegated,
                List.of(assignment(20L, 10L, 7L)), 11L));
        assertFalse((Boolean) invoke(service, "isDelegatedTenantContext", delegated, List.of(active), 99L));
        assertTrue((Boolean) invoke(service, "isTenantSuperRole", parentSuper));
        RoleBO ordinary = role(8L, 10L, "user", "User");
        ordinary.setDelFlag(1);
        assertFalse((Boolean) invoke(service, "isTenantSuperRole", ordinary));
        assertFalse((Boolean) invoke(service, "isTenantSuperRole", (Object) null));

        Map<String, Object> ext = invoke(service, "buildExtInfo", "{\"existing\":true}", parentSuper,
                10L, LocalDateTime.now(), "127.0.0.1");
        assertEquals(true, ext.get("existing"));
        assertTrue(ext.containsKey("currentRole"));
        assertFalse(((Map<?, ?>) invoke(service, "buildExtInfo", "{}", null, null,
                LocalDateTime.now(), null)).isEmpty());

        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(validTenant);
        when(tenantRoles.selectRoleById(3L)).thenReturn(role(3L, 10L, "editor", "Editor"));
        List<UserScopeRoleBO> mixed = Arrays.asList(active, active, inactive, noTenant);
        assertEquals(1, ((List<?>) invoke(service, "buildSubTenantList", mixed, 10L)).size());
        assertTrue(((List<?>) invoke(service, "buildSubTenantList", null, null)).isEmpty());
        assertEquals(1, ((List<?>) invoke(service, "buildTenantList", mixed)).size());
        assertTrue(((List<?>) invoke(service, "buildTenantList", (Object) null)).isEmpty());
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L, 11L));
        when(tenants.selectAll()).thenReturn(Arrays.asList(validTenant, disabledTenant, deletedTenant,
                new TenantBO()));
        List<?> scoped = invoke(service, "buildScopedTenantList", 10L, 10L);
        assertEquals(1, scoped.size());
        assertTrue(((List<?>) invoke(service, "buildScopedTenantList", 10L, null)).size() >= 1);

        TenantBO child = tenant(11L, "child", "Child");
        child.setParentId(10L);
        when(tenants.selectAll()).thenReturn(Arrays.asList(validTenant, child));
        assertEquals("Tenant 10 / Child", invoke(service, "buildTenantPath", 11L));
        child.setParentId(11L);
        assertEquals("Child", invoke(service, "buildTenantPath", 11L));

        RoleBO inactiveRole = role(9L, 10L, "inactive", "Inactive");
        inactiveRole.setStatus(0);
        assertEquals(parentSuper, invoke(service, "resolveCurrentRoleForTenant", 7L,
                Arrays.asList(parentSuper, inactiveRole), List.of(assignment(20L, 10L, 7L)), 10L));
        assertNull(invoke(service, "resolveCurrentRoleForTenant", null, List.of(parentSuper), List.of(active), null));
        assertNull(invoke(service, "resolveCurrentRoleForTenant", 99L, List.of(inactiveRole), List.of(active), 10L));
        assertEquals(parentSuper, invoke(service, "resolveCurrentRoleForTenant", 99L,
                List.of(parentSuper), List.of(assignment(20L, 10L, 7L)), 10L));
    }

    @Test
    void coversTenantSwitchDelegationAndMissingRoleBoundaries() {
        UserBO parent = user(40L, "parent", "secret", 1);
        parent.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":11}");
        RoleBO parentSuper = role(70L, 10L, "tenant_super", "Parent Admin");
        TenantBO child = tenant(12L, "child", "Child");
        when(users.selectById(40L)).thenReturn(parent);
        when(assignments.selectByUserId(40L)).thenReturn(List.of(assignment(40L, 10L, 70L)));
        when(tenantRoles.selectTenantById(new TenantId(12L))).thenReturn(child);
        when(tenantRoles.selectRoleById(70L)).thenReturn(parentSuper);
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(11L, 12L));
        when(tenants.selectDescendantIds(new TenantId(11L))).thenReturn(List.of(11L, 12L));
        when(tenantRoles.selectTenantSuperRole(new TenantId(12L))).thenReturn(role(72L, 12L, "tenant_super", "Child Admin"));
        when(users.update(parent)).thenReturn(true);
        assertTrue(service.switchCurrentTenant(40L, new TenantId(12L)));

        UserBO outside = user(41L, "outside", "secret", 1);
        outside.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":11}");
        when(users.selectById(41L)).thenReturn(outside);
        when(assignments.selectByUserId(41L)).thenReturn(List.of(assignment(41L, 10L, 70L)));
        when(tenants.selectDescendantIds(new TenantId(11L))).thenReturn(List.of(11L));
        assertFalse(service.switchCurrentTenant(41L, new TenantId(12L)));

        UserBO regular = user(42L, "regular", "secret", 1);
        regular.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":10}");
        when(users.selectById(42L)).thenReturn(regular);
        when(assignments.selectByUserId(42L)).thenReturn(List.of());
        when(tenantRoles.selectTenantById(new TenantId(12L))).thenReturn(child);
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L, 12L));
        assertFalse(service.switchCurrentTenant(42L, new TenantId(12L)));
    }

    @Test
    void coversAuthenticationRecoveryAndTenantSelectionEdges() {
        UserBO nullStatus = user(50L, "null-status", "secret", 1);
        nullStatus.setStatus(null);
        when(users.selectActiveUserByUsername("null-status")).thenReturn(nullStatus);
        assertNull(service.login("null-status", "secret"));

        when(users.selectByUsername("taken")).thenReturn(user(51L, "taken", "secret", 1));
        assertThrows(IllegalArgumentException.class, () -> service.register("taken", "secret", "x@y"));

        when(users.selectByUsername("new")).thenReturn(null);
        doAnswer(invocation -> { invocation.<UserBO>getArgument(0).setId(52L); return null; })
                .when(users).insert(any(UserBO.class));
        when(users.selectById(52L)).thenReturn(user(52L, "new", "secret", 1));
        when(tenantRoles.selectEnabledRoleByCode(new TenantId(1L), "user")).thenReturn(null);
        when(users.selectActiveUserByUsername("new")).thenReturn(null);
        assertThrows(IllegalStateException.class,
                () -> service.register("new", "secret", "x@y"));

        when(captcha.validateCaptcha("key", "code")).thenReturn(true);
        UserBO existing = user(53L, "phone", "secret", 1);
        when(users.selectByUsername("phone")).thenReturn(existing);
        assertNull(service.codeLogin("phone", "code", "key"));
        when(users.selectByEmail("missing@y")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.forgetPassword("missing@y", "new", "code", "key"));

        UserBO recover = user(54L, "recover", "secret", 1);
        when(users.selectByEmail("recover@y")).thenReturn(recover);
        when(users.update(recover)).thenReturn(false, true);
        assertFalse(service.forgetPassword("recover@y", "new", "code", "key"));
        SaTokenHelper helper = mock(SaTokenHelper.class);
        try (var mocked = mockStatic(SaTokenHelper.class)) {
            mocked.when(SaTokenHelper::getInstance).thenReturn(helper);
            assertTrue(service.forgetPassword("recover@y", "new", "code", "key"));
            verify(helper).logout(54L);
        }

        // A tenant-super user can return home, use a preferred child role, or be rejected
        // when the target tenant is inactive/outside the currently allowed subtree.
        UserBO admin = user(60L, "admin", "secret", 1);
        admin.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":11,\"currentRole\":{\"roleId\":71}}");
        RoleBO homeSuper = role(70L, 10L, "tenant_super", "Home");
        RoleBO childRole = role(71L, 12L, "editor", "Child");
        when(users.selectById(60L)).thenReturn(admin);
        when(assignments.selectByUserId(60L)).thenReturn(List.of(assignment(60L, 10L, 70L), assignment(60L, 12L, 71L)));
        when(tenantRoles.selectTenantById(new TenantId(10L))).thenReturn(tenant(10L, "home", "Home"));
        when(tenantRoles.selectTenantById(new TenantId(12L))).thenReturn(tenant(12L, "child", "Child"));
        when(tenantRoles.selectRoleById(70L)).thenReturn(homeSuper);
        when(tenantRoles.selectRoleById(71L)).thenReturn(childRole);
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L, 12L));
        when(tenants.selectDescendantIds(new TenantId(11L))).thenReturn(List.of(11L, 12L));
        when(users.update(admin)).thenReturn(true);
        assertTrue(service.switchCurrentTenant(60L, new TenantId(12L)));
        assertTrue(service.switchCurrentTenant(60L, new TenantId(10L)));

        TenantBO inactiveTenant = tenant(13L, "inactive", "Inactive");
        inactiveTenant.setStatus(0);
        when(tenantRoles.selectTenantById(new TenantId(13L))).thenReturn(inactiveTenant);
        assertFalse(service.switchCurrentTenant(60L, new TenantId(13L)));

        ActorContext actor = new ActorContext(new TenantId(10L), new UserId(60L), false);
        admin.setExtInfo("{\"homeTenantId\":10,\"currentTenantId\":10}");
        when(assignments.selectByUserId(60L)).thenReturn(List.of(assignment(60L, 10L, 70L)));
        when(users.selectById(60L)).thenReturn(admin);
        when(tenants.selectAll()).thenReturn(List.of(tenant(10L, "home", "Home")));
        when(tenants.selectDescendantIds(new TenantId(10L))).thenReturn(List.of(10L));
        assertEquals(1, service.getUserTenants(actor, 60L).size());
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(AuthServiceImpl service, String name, Object... args) throws Exception {
        Class<?>[] types = Arrays.stream(args).map(value -> value == null ? Object.class : value.getClass()).toArray(Class<?>[]::new);
        for (var method : AuthServiceImpl.class.getDeclaredMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
            method.setAccessible(true);
            try {
                return (T) method.invoke(service, args);
            } catch (IllegalArgumentException ignored) {
                // Keep looking when a null argument made the erased signature ambiguous.
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static UserBO user(Long id, String username, String password, int status) {
        UserBO value = new UserBO();
        value.setId(id); value.setUsername(username); value.setPassword(com.shiyu.ai.common.core.utils.PasswordUtils.encode(password)); value.setStatus(status);
        return value;
    }

    private static RoleBO role(Long id, Long tenantId, String code, String name) {
        RoleBO value = new RoleBO(); value.setId(id); value.setTenantId(tenantId); value.setCode(code); value.setName(name); value.setStatus(1); value.setDelFlag(0); return value;
    }

    private static TenantBO tenant(Long id, String code, String name) {
        TenantBO value = new TenantBO(); value.setId(id); value.setCode(code); value.setName(name); value.setStatus(1); value.setDelFlag(0); return value;
    }

    private static UserScopeRoleBO assignment(Long userId, Long tenantId, Long roleId) {
        UserScopeRoleBO value = new UserScopeRoleBO(); value.setUserId(userId); value.setTenantId(tenantId); value.setRoleId(roleId); value.setStatus(1); value.setDelFlag(0); return value;
    }
}

package com.shiyu.ai.iam.implementation.application.identity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.iam.implementation.port.repository.TenantRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** 验证保存的租户上下文必须由当前有效的授权关系和租户树支持。 */
class AuthTenantContextSupportBoundaryTest {
    @Test
    void incompleteHistoricalGrantsNeverBecomeActiveAssignments() {
        AuthTenantContextSupport support = new AuthTenantContextSupport(
                mock(TenantRepository.class), mock(TenantRoleRepository.class));
        UserScopeRoleBO grant = assignment(10L, 30L);
        assertFalse(support.isActiveAssignment(null));
        grant.setStatus(null);
        assertFalse(support.isActiveAssignment(grant));
        grant.setStatus(1);
        grant.setDelFlag(1);
        assertFalse(support.isActiveAssignment(grant));
        grant.setDelFlag(0);
        grant.setTenantId(null);
        assertFalse(support.isActiveAssignment(grant));
        grant.setTenantId(10L);
        grant.setRoleId(null);
        assertFalse(support.isActiveAssignment(grant));
        assertFalse(support.hasTenantAssignment(List.of(grant), 10L));
        assertNull(support.resolveCurrentTenantId("{invalid", List.of(grant)));
    }

    @Test
    void scopedTenantListExcludesInvisibleAndDisabledTenantsAndBuildsPaths() {
        TenantRepository tenants = mock(TenantRepository.class);
        AuthTenantContextSupport support = new AuthTenantContextSupport(
                tenants, mock(TenantRoleRepository.class));
        TenantBO root = tenant(10L);
        root.setName("Root");
        TenantBO child = tenant(20L);
        child.setName("Child");
        child.setParentId(10L);
        TenantBO returned = tenant(30L);
        returned.setName("Return");
        TenantBO hidden = tenant(40L);
        hidden.setStatus(0);
        when(tenants.selectDescendantIds(new TenantId(10L)))
                .thenReturn(List.of(10L, 20L, 40L));
        when(tenants.selectAll()).thenReturn(List.of(root, child, returned, hidden));

        var visible = support.buildScopedTenantList(10L, 30L);
        assertEquals(List.of(10L, 20L, 30L), visible.stream()
                .map(item -> item.getId()).toList());
        assertEquals("Root / Child", visible.get(1).getPathName());
        assertEquals("", support.buildTenantPath(99L));
    }

    @Test
    void savedTenantWithRevokedRoleFallsBackToAnotherLiveAssignment() {
        TenantRoleRepository roles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport support = new AuthTenantContextSupport(
                mock(TenantRepository.class), roles);
        UserScopeRoleBO stale = assignment(20L, 40L);
        UserScopeRoleBO home = assignment(10L, 30L);
        RoleBO staleRole = role(40L, 20L, "member");
        staleRole.setStatus(0);
        when(roles.selectRoleById(40L)).thenReturn(staleRole);
        when(roles.selectRoleById(30L)).thenReturn(role(30L, 10L, "member"));

        assertFalse(support.hasTenantAssignment(List.of(stale), 20L));
        assertEquals(10L, support.resolveCurrentTenantId(
                "{\"currentTenantId\":20}", List.of(stale, home)));

        staleRole.setStatus(1);
        staleRole.setTenantId(99L);
        assertFalse(support.hasTenantAssignment(List.of(stale), 20L));
        assertEquals(10L, support.resolveCurrentTenantId(null, List.of(stale, home)));
    }

    @Test
    void selectableTenantsRequireALiveRoleOwnedByEachAssignedTenant() {
        TenantRoleRepository roles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport support = new AuthTenantContextSupport(
                mock(TenantRepository.class), roles);
        UserScopeRoleBO valid = assignment(10L, 30L);
        UserScopeRoleBO foreign = assignment(20L, 40L);
        UserScopeRoleBO disabled = assignment(30L, 50L);
        UserScopeRoleBO missing = assignment(40L, 60L);
        when(roles.selectRoleById(30L)).thenReturn(role(30L, 10L, "member"));
        when(roles.selectRoleById(40L)).thenReturn(role(40L, 99L, "foreign"));
        RoleBO disabledRole = role(50L, 30L, "member");
        disabledRole.setStatus(0);
        when(roles.selectRoleById(50L)).thenReturn(disabledRole);
        when(roles.selectTenantById(new TenantId(10L))).thenReturn(tenant(10L));
        when(roles.selectTenantById(new TenantId(20L))).thenReturn(tenant(20L));
        when(roles.selectTenantById(new TenantId(30L))).thenReturn(tenant(30L));
        when(roles.selectTenantById(new TenantId(40L))).thenReturn(tenant(40L));

        var tenants = support.buildTenantList(List.of(valid, foreign, disabled, missing));

        assertEquals(1, tenants.size());
        assertEquals(10L, tenants.get(0).getId());
    }

    @Test
    void subTenantContextUsesOnlyLiveGrantsAndRolesOwnedByTheirTenant() {
        TenantRoleRepository roles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport support = new AuthTenantContextSupport(
                mock(TenantRepository.class), roles);
        UserScopeRoleBO revoked = assignment(10L, 30L);
        revoked.setStatus(0);
        UserScopeRoleBO foreign = assignment(10L, 40L);
        UserScopeRoleBO otherTenant = assignment(20L, 41L);
        UserScopeRoleBO valid = assignment(10L, 31L);
        TenantBO home = tenant(10L);
        TenantBO child = tenant(20L);
        when(roles.selectTenantById(new TenantId(10L))).thenReturn(home);
        when(roles.selectTenantById(new TenantId(20L))).thenReturn(child);
        when(roles.selectRoleById(30L)).thenReturn(role(30L, 10L, "revoked"));
        when(roles.selectRoleById(40L)).thenReturn(role(40L, 99L, "foreign"));
        when(roles.selectRoleById(41L)).thenReturn(role(41L, 99L, "foreign"));
        when(roles.selectRoleById(31L)).thenReturn(role(31L, 10L, "member"));

        var contexts = support.buildSubTenantList(
                List.of(revoked, foreign, otherTenant, valid), null);

        assertEquals(1, contexts.size());
        assertEquals(10L, contexts.get(0).getTenantId());
        assertEquals("member", contexts.get(0).getRoleCode());
        assertEquals(1, support.buildSubTenantList(
                List.of(revoked, foreign, otherTenant, valid), 10L).size());
        assertTrue(support.buildSubTenantList(
                List.of(revoked, foreign, otherTenant, valid), 20L).isEmpty());
    }

    @Test
    void selectedRoleMustStillBeActiveAndOwnedByTheCurrentTenant() {
        AuthTenantContextSupport support = new AuthTenantContextSupport(
                mock(TenantRepository.class), mock(TenantRoleRepository.class));
        UserScopeRoleBO validGrant = assignment(10L, 30L);
        UserScopeRoleBO foreignGrant = assignment(10L, 31L);
        UserScopeRoleBO disabledGrant = assignment(10L, 32L);
        RoleBO valid = role(30L, 10L, "member");
        RoleBO foreign = role(31L, 99L, "tenant_super");
        RoleBO disabled = role(32L, 10L, "manager");
        disabled.setStatus(0);
        List<RoleBO> roles = List.of(foreign, disabled, valid);
        List<UserScopeRoleBO> grants = List.of(validGrant, foreignGrant, disabledGrant);

        assertEquals(valid, support.resolveCurrentRoleForTenant(31L, roles, grants, 10L));
        assertEquals(valid, support.resolveCurrentRoleForTenant(32L, roles, grants, 10L));
    }

    @Test
    void delegatedContextRequiresLiveHomeSuperRoleAndHomeDescendant() {
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository roles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport support = new AuthTenantContextSupport(tenants, roles);
        UserScopeRoleBO grant = assignment(10L, 30L);
        RoleBO homeSuper = role(30L, 10L, "tenant_super");
        Map<String, Object> saved = Map.of(
                "switchMode", "PARENT_SUPER_ADMIN", "homeTenantId", 10L);
        when(tenants.selectDescendantIds(new TenantId(10L)))
                .thenReturn(List.of(10L, 20L));
        when(roles.selectRoleById(30L)).thenReturn(homeSuper);
        RoleBO childSuper = role(40L, 20L, "tenant_super");
        when(roles.selectTenantSuperRole(new TenantId(20L))).thenReturn(childSuper);

        assertTrue(support.isDelegatedTenantContext(saved, List.of(grant), 20L));
        assertFalse(support.isDelegatedTenantContext(saved, List.of(grant), 99L));
        assertFalse(support.isDelegatedTenantContext(
                Map.of("switchMode", "NORMAL", "homeTenantId", 10L),
                List.of(grant), 20L));

        grant.setStatus(0);
        assertFalse(support.isDelegatedTenantContext(saved, List.of(grant), 20L));
        grant.setStatus(1);
        homeSuper.setTenantId(99L);
        assertFalse(support.isDelegatedTenantContext(saved, List.of(grant), 20L));
        homeSuper.setTenantId(10L);
        homeSuper.setStatus(0);
        assertFalse(support.isDelegatedTenantContext(saved, List.of(grant), 20L));
        homeSuper.setStatus(1);
        childSuper.setTenantId(99L);
        assertFalse(support.isDelegatedTenantContext(saved, List.of(grant), 20L));
        childSuper.setTenantId(20L);
        childSuper.setStatus(0);
        assertFalse(support.isDelegatedTenantContext(saved, List.of(grant), 20L));
    }

    @Test
    void forgedSavedTenantFallsBackToAssignedHomeTenant() {
        TenantRepository tenants = mock(TenantRepository.class);
        TenantRoleRepository roles = mock(TenantRoleRepository.class);
        AuthTenantContextSupport support = new AuthTenantContextSupport(tenants, roles);
        UserScopeRoleBO grant = assignment(10L, 30L);
        String saved = "{\"homeTenantId\":10,\"currentTenantId\":99,"
                + "\"switchMode\":\"PARENT_SUPER_ADMIN\"}";
        when(tenants.selectDescendantIds(new TenantId(10L)))
                .thenReturn(List.of(10L, 20L));
        when(roles.selectRoleById(30L))
                .thenReturn(role(30L, 10L, "tenant_super"));
        RoleBO childSuper = role(40L, 20L, "tenant_super");
        when(roles.selectTenantSuperRole(new TenantId(20L))).thenReturn(childSuper);

        assertEquals(10L, support.resolveCurrentTenantId(saved, List.of(grant)));
        assertEquals(20L, support.resolveCurrentTenantId(
                saved.replace("99", "20"), List.of(grant)));
        childSuper.setStatus(0);
        assertEquals(10L, support.resolveCurrentTenantId(
                saved.replace("99", "20"), List.of(grant)));
        assertEquals(10L, support.resolveCurrentTenantId(
                "{\"currentTenantId\":10}", List.of(grant)));
    }

    private static UserScopeRoleBO assignment(Long tenantId, Long roleId) {
        UserScopeRoleBO value = new UserScopeRoleBO();
        value.setTenantId(tenantId);
        value.setRoleId(roleId);
        value.setStatus(1);
        value.setDelFlag(0);
        return value;
    }

    private static RoleBO role(Long id, Long tenantId, String code) {
        RoleBO value = new RoleBO();
        value.setId(id);
        value.setTenantId(tenantId);
        value.setCode(code);
        value.setStatus(1);
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

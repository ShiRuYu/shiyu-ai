package com.shiyu.ai.iam.implementation.application.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.RoleId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import com.shiyu.ai.iam.implementation.port.repository.AuthRepository;
import com.shiyu.ai.iam.implementation.port.repository.TenantRoleRepository;
import com.shiyu.ai.iam.implementation.port.repository.UserScopeRoleRepository;
import com.shiyu.ai.iam.implementation.domain.model.TenantBO;
import com.shiyu.ai.iam.implementation.domain.model.RoleBO;
import com.shiyu.ai.iam.implementation.domain.model.UserScopeRoleBO;
import com.shiyu.ai.kernel.context.TenantScope;
import java.util.List;

/**
 * 验证 平台 用量 Access Impl 相关功能、边界条件、异常路径和协作行为。
 */
class PlatformUsageAccessImplTest {

    private final TenantRoleRepository roles = mock(TenantRoleRepository.class);
    private final UserScopeRoleRepository assignments = mock(UserScopeRoleRepository.class);
    private final AuthRepository permissions = mock(AuthRepository.class);
    private final PlatformUsageAccessImpl access = new PlatformUsageAccessImpl(roles, assignments, permissions);

    @Test
    void revalidatesTenantRoleAssignmentAndDedicatedPermission() {
        var tenant = new TenantBO();
        tenant.setId(1L);
        tenant.setStatus(1);
        tenant.setDelFlag(0);
        var role = new RoleBO();
        role.setId(1L);
        role.setTenantId(1L);
        role.setCode("super");
        role.setStatus(1);
        role.setDelFlag(0);
        var assignment = new UserScopeRoleBO();
        assignment.setUserId(7L);
        assignment.setTenantId(1L);
        assignment.setRoleId(1L);
        assignment.setStatus(1);
        assignment.setDelFlag(0);
        var tenantId = new TenantId(1L);
        var actor = new ActorContext(tenantId, new UserId(7L), new RoleId(1L),
                "super", tenantId, null, false);
        when(roles.selectTenantById(tenantId)).thenReturn(tenant);
        when(roles.selectEnabledRoleByCode(tenantId, "super")).thenReturn(role);
        when(assignments.selectByUserIds(List.of(7L))).thenReturn(List.of(assignment));
        when(permissions.selectCodesByRoleId(1L, tenantId)).thenReturn(List.of("platform:usage:read"));
        TenantScope.withTenant(tenantId, () -> {
            assertTrue(access.canReadPlatformUsage(actor));
            assignment.setStatus(0);
            assertFalse(access.canReadPlatformUsage(actor));
            assignment.setStatus(1);
            assignment.setDelFlag(1);
            assertFalse(access.canReadPlatformUsage(actor));
            assignment.setDelFlag(0);
            role.setStatus(0);
            assertFalse(access.canReadPlatformUsage(actor));
            role.setStatus(1);
            tenant.setStatus(0);
            assertFalse(access.canReadPlatformUsage(actor));
            tenant.setStatus(1);
            when(permissions.selectCodesByRoleId(1L, tenantId)).thenReturn(List.of());
            assertFalse(access.canReadPlatformUsage(actor));
            return null;
        });
    }

    @Test
    void rejectsUnverifiedDefaultTenantSuperAdministrator() {
        ActorContext actor = new ActorContext(
                new TenantId(1L),
                new UserId(7L),
                new RoleId(1L),
                "super",
                new TenantId(1L),
                null,
                true);

        assertFalse(access.canReadPlatformUsage(actor));
    }

    @Test
    void rejectsChildTenantAndDelegatedIdentities() {
        ActorContext childTenant = new ActorContext(
                new TenantId(2L),
                new UserId(7L),
                new RoleId(1L),
                "super",
                new TenantId(1L),
                null,
                true);
        ActorContext delegated = new ActorContext(
                new TenantId(1L),
                new UserId(7L),
                new RoleId(1L),
                "super",
                new TenantId(1L),
                "PARENT_SUPER_ADMIN",
                true);

        assertFalse(access.canReadPlatformUsage(childTenant));
        assertFalse(access.canReadPlatformUsage(delegated));
        assertFalse(access.canReadPlatformUsage(null));
    }
}

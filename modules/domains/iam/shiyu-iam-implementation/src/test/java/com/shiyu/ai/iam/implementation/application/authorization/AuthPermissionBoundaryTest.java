package com.shiyu.ai.iam.implementation.application.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shiyu.ai.iam.implementation.port.repository.AuthRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 验证查询用户权限时不会越过当前租户与当前角色的边界。 */
class AuthPermissionBoundaryTest {
    private final AuthRepository repository = mock(AuthRepository.class);
    private final AuthPermissionUseCase permissions = new AuthPermissionUseCase(repository);
    private final TenantId tenant = new TenantId(7L);
    private final UserId owner = new UserId(11L);
    private final UserId other = new UserId(12L);

    @Test
    void ordinaryUserCannotReadAnotherUsersPermissions() {
        ActorContext actor = new ActorContext(tenant, owner, false);

        assertThat(permissions.getAuthCodesByUserId(actor, other)).isEmpty();

        verifyNoInteractions(repository);
    }

    @Test
    void selectedRoleLimitsSelfPermissionLookup() {
        ActorContext actor = new ActorContext(tenant, owner, null, "learner", tenant, null, false);
        when(repository.selectCodesByUserIdAndRoleCode(owner, tenant, "learner"))
                .thenReturn(List.of("learning:read"));

        assertThat(permissions.getAuthCodesByUserId(actor, owner))
                .containsExactly("learning:read");

        verify(repository).selectCodesByUserIdAndRoleCode(owner, tenant, "learner");
    }

    @Test
    void delegatedSuperAdminUsesRoleInCurrentTenantRatherThanHomeUserAssignment() {
        ActorContext actor = new ActorContext(
                tenant, owner, null, "tenant_super", new TenantId(1L),
                "PARENT_SUPER_ADMIN", false);
        when(repository.selectCodesByRoleCodeAndTenant("tenant_super", tenant))
                .thenReturn(List.of("child:manage"));

        assertThat(permissions.getAuthCodesByUserId(actor, other))
                .containsExactly("child:manage");

        verify(repository).selectCodesByRoleCodeAndTenant("tenant_super", tenant);
    }

    @Test
    void platformAdminCanInspectUserInCurrentTenantWithoutSelectedRole() {
        ActorContext actor = new ActorContext(tenant, owner, true);
        when(repository.selectCodesByUserId(other, tenant)).thenReturn(List.of("agent:read"));

        assertThat(permissions.getAuthCodesByUserId(actor, other))
                .containsExactly("agent:read");

        verify(repository).selectCodesByUserId(other, tenant);
    }

    @Test
    void missingIdentityOrRepositoryFailureCannotGrantPermissions() {
        ActorContext actor = new ActorContext(tenant, owner, false);
        assertThat(permissions.getAuthCodesByUserId(null, owner)).isEmpty();
        assertThat(permissions.getAuthCodesByUserId(actor, null)).isEmpty();
        verifyNoInteractions(repository);

        when(repository.selectCodesByUserId(owner, tenant))
                .thenThrow(new IllegalStateException("lookup unavailable"));
        assertThat(permissions.getAuthCodesByUserId(actor, owner)).isEmpty();
        verify(repository).selectCodesByUserId(owner, tenant);
    }

    @Test
    void missingPermissionRowsReturnEmptyForSelectedRole() {
        ActorContext actor = new ActorContext(tenant, owner, null, "learner", tenant, null, false);
        when(repository.selectCodesByUserIdAndRoleCode(owner, tenant, "learner"))
                .thenReturn(null);

        assertThat(permissions.getAuthCodesByUserId(actor, owner)).isEmpty();
        verify(repository).selectCodesByUserIdAndRoleCode(owner, tenant, "learner");
    }

    @Test
    void usernamePermissionLookupStaysWithinActorTenantAndFailsClosed() {
        ActorContext actor = new ActorContext(tenant, owner, false);
        assertThat(permissions.getAuthCodes(null, "alice")).isEmpty();
        verifyNoInteractions(repository);

        when(repository.selectCodesByUsername("alice", tenant))
                .thenReturn(List.of("agent:read"))
                .thenReturn(List.of())
                .thenReturn(null)
                .thenThrow(new IllegalStateException("lookup unavailable"));
        assertThat(permissions.getAuthCodes(actor, "alice"))
                .containsExactly("agent:read");
        assertThat(permissions.getAuthCodes(actor, "alice")).isEmpty();
        assertThat(permissions.getAuthCodes(actor, "alice")).isEmpty();
        assertThat(permissions.getAuthCodes(actor, "alice")).isEmpty();
    }
}

package com.shiyu.ai.auth.persistence.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.domain.model.UserBO;
import com.shiyu.ai.auth.domain.model.UserScopeRoleBO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.TenantDO;
import com.shiyu.ai.auth.persistence.dataobject.UserDO;
import com.shiyu.ai.auth.persistence.dataobject.UserScopeRoleDO;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.auth.persistence.mapper.TenantMapper;
import com.shiyu.ai.auth.persistence.mapper.UserMapper;
import com.shiyu.ai.auth.persistence.mapper.UserScopeRoleMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthUserLookupRepositoryImplTest {
    private UserMapper users;
    private UserScopeRoleMapper assignments;
    private RoleMapper roles;
    private TenantMapper tenants;
    private AuthUserLookupRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        users = mock(UserMapper.class);
        assignments = mock(UserScopeRoleMapper.class);
        roles = mock(RoleMapper.class);
        tenants = mock(TenantMapper.class);
        repository = new AuthUserLookupRepositoryImpl();
        ReflectionTestUtils.setField(repository, "userMapper", users);
        ReflectionTestUtils.setField(repository, "userScopeRoleMapper", assignments);
        ReflectionTestUtils.setField(repository, "roleMapper", roles);
        ReflectionTestUtils.setField(repository, "tenantMapper", tenants);
    }

    @Test
    void validatesUpdatesAndEmptyRoleInputs() {
        assertFalse(repository.updateUserExtInfo(null, "{}"));
        assertEquals(List.of(), repository.selectRolesByIds(null));
        assertEquals(List.of(), repository.selectRolesByIds(Set.of()));
        assertThrows(IllegalArgumentException.class, () -> repository.selectTenantSuperRole(null));
        verifyNoInteractions(users, roles, tenants);
    }

    @Test
    void mapsUserRoleAndTenantLookupsWithExplicitTenantBypass() {
        UserDO userRow = new UserDO(); userRow.setId(7L); userRow.setUsername("alice");
        RoleDO roleRow = new RoleDO(); roleRow.setId(3L); roleRow.setCode("editor");
        TenantDO tenantRow = new TenantDO(); tenantRow.setId(9L); tenantRow.setName("Tenant");
        UserScopeRoleDO scopeRow = new UserScopeRoleDO();
        UserBO user = new UserBO(); user.setId(7L);
        RoleBO role = new RoleBO(); role.setId(3L);
        TenantBO tenant = new TenantBO(); tenant.setId(9L);
        UserScopeRoleBO scope = new UserScopeRoleBO();
        when(users.selectOneById(7L)).thenReturn(userRow);
        when(users.update(any(UserDO.class))).thenReturn(1);
        when(assignments.selectByUserId(7L)).thenReturn(List.of(scopeRow));
        when(roles.selectOneById(3L)).thenReturn(roleRow);
        when(roles.selectOneByQuery(any())).thenReturn(roleRow);
        when(roles.selectListByQuery(any())).thenReturn(List.of(roleRow));
        when(tenants.selectOneById(9L)).thenReturn(tenantRow);

        try (MockedStatic<MapstructUtils> mapped = mockStatic(MapstructUtils.class)) {
            mapped.when(() -> MapstructUtils.convert(userRow, UserBO.class)).thenReturn(user);
            mapped.when(() -> MapstructUtils.convert(roleRow, RoleBO.class)).thenReturn(role);
            mapped.when(() -> MapstructUtils.convert(tenantRow, TenantBO.class)).thenReturn(tenant);
            mapped.when(() -> MapstructUtils.convert(anyList(), eq(UserScopeRoleBO.class))).thenReturn(List.of(scope));
            mapped.when(() -> MapstructUtils.convert(anyList(), eq(RoleBO.class))).thenReturn(List.of(role));

            assertSame(user, repository.selectUserById(7L));
            assertTrue(repository.updateUserExtInfo(7L, "{\"theme\":\"dark\"}"));
            assertEquals(List.of(scope), repository.selectUserScopeRoles(7L));
            assertSame(role, repository.selectRoleById(3L));
            assertSame(role, repository.selectTenantSuperRole(new TenantId(9L)));
            assertEquals(List.of(role), repository.selectRolesByIds(Set.of(3L)));
            assertSame(tenant, repository.selectTenantById(new TenantId(9L)));
        }
        when(users.update(any(UserDO.class))).thenReturn(0);
        assertFalse(repository.updateUserExtInfo(7L, "{}"));
        verify(users, times(2)).update(any(UserDO.class));
        verify(assignments).selectByUserId(7L);
        verify(roles).selectOneByQuery(any());
    }
}

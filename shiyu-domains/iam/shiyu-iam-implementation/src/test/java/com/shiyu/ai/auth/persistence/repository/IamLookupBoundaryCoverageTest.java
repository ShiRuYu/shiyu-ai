package com.shiyu.ai.auth.persistence.repository;

import com.shiyu.ai.auth.port.repository.AuthUserLookupRepository;
import com.shiyu.ai.auth.port.repository.TenantRepository;
import com.shiyu.ai.auth.persistence.mapper.UserMapper;
import com.shiyu.ai.auth.persistence.mapper.UserScopeRoleMapper;
import com.shiyu.ai.auth.service.AuthContextService;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IamLookupBoundaryCoverageTest {
    @Test
    void coversScopeRoleRepositoryQueriesAndMutations() {
        UserScopeRoleMapper mapper = mock(UserScopeRoleMapper.class);
        when(mapper.selectByUserId(8L)).thenReturn(List.of());
        when(mapper.selectByUserIds(List.of(8L))).thenReturn(List.of());
        UserScopeRoleRepositoryImpl repository = new UserScopeRoleRepositoryImpl();
        ReflectionTestUtils.setField(repository, "userScopeRoleMapper", mapper);
        assertEquals(List.of(), repository.selectByUserId(8L));
        assertEquals(List.of(), repository.selectByUserIds(List.of(8L)));
        repository.insert(null);
        repository.deleteByUserIdAndTenantId(8L, new TenantId(7L));
        repository.deleteByUserIdRoleIdAndTenantId(8L, 3L, new TenantId(7L));
        verify(mapper).insertSelective(isNull());
        verify(mapper, times(2)).deleteByQuery(any());
    }

    @Test
    void coversAuthContextAndSaTokenLookupBoundariesWithMissingRows() {
        AuthUserLookupRepository lookup = mock(AuthUserLookupRepository.class);
        TenantRepository tenants = mock(TenantRepository.class);
        when(tenants.selectDescendantIds(new TenantId(7L))).thenReturn(List.of());
        AuthContextService context = new AuthContextService(lookup, tenants);
        assertNull(context.user(8L));
        assertNull(context.tenant(7L));
        assertNull(context.tenant(null));
        assertNull(context.role(3L));
        assertNull(context.tenantSuperRole(new TenantId(7L)));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> context.tenantSuperRole(null));
        assertEquals(List.of(), context.scopeRoles(8L));
        assertEquals(List.of(), context.descendantTenantIds(new TenantId(7L)));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> context.descendantTenantIds(null));

        UserMapper mapper = mock(UserMapper.class);
        SaTokenUserRepositoryImpl repository = new SaTokenUserRepositoryImpl();
        ReflectionTestUtils.setField(repository, "userMapper", mapper);
        assertNull(repository.selectById(8L));
        repository.updateExtInfo(null);
        verify(mapper).update(isNull());
    }
}

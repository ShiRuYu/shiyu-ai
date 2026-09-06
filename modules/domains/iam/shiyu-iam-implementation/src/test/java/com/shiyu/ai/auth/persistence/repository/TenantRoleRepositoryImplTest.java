package com.shiyu.ai.auth.persistence.repository;

import com.shiyu.ai.auth.domain.model.RoleBO;
import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.auth.persistence.dataobject.RoleDO;
import com.shiyu.ai.auth.persistence.dataobject.TenantDO;
import com.shiyu.ai.auth.persistence.mapper.RoleMapper;
import com.shiyu.ai.auth.persistence.mapper.TenantMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TenantRoleRepositoryImplTest {
    private TenantMapper tenants;
    private RoleMapper roles;
    private TenantRoleRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        tenants = mock(TenantMapper.class);
        roles = mock(RoleMapper.class);
        repository = new TenantRoleRepositoryImpl();
        ReflectionTestUtils.setField(repository, "tenantMapper", tenants);
        ReflectionTestUtils.setField(repository, "roleMapper", roles);
    }

    @Test
    void rejectsMissingRoleArgumentsAndMissingTenantSuperRole() {
        assertThrows(IllegalArgumentException.class, () -> repository.selectEnabledRoleByCode(null, "editor"));
        assertNull(repository.selectEnabledRoleByCode(new TenantId(7L), null));
        assertNull(repository.selectEnabledRoleByCode(new TenantId(7L), " "));
        assertThrows(IllegalArgumentException.class, () -> repository.selectTenantSuperRole(null));
        assertThrows(IllegalArgumentException.class, () -> repository.selectTenantSuperRole(new TenantId(0L)));
        assertEquals("Unknown", repository.selectTenantNameById(new TenantId(7L)));
        verifyNoInteractions(roles);
    }

    @Test
    void mapsTenantAndRoleQueriesAndUnknownTenantName() {
        TenantDO tenantRow = new TenantDO(); tenantRow.setId(7L); tenantRow.setName("ShiYu");
        RoleDO roleRow = new RoleDO(); roleRow.setId(3L); roleRow.setCode("editor");
        TenantBO tenant = new TenantBO(); tenant.setId(7L);
        RoleBO role = new RoleBO(); role.setId(3L);
        when(tenants.selectOneById(7L)).thenReturn(tenantRow);
        when(roles.selectOneById(3L)).thenReturn(roleRow);
        when(roles.selectOneByQuery(any())).thenReturn(roleRow);
        try (MockedStatic<MapstructUtils> mapped = mockStatic(MapstructUtils.class)) {
            mapped.when(() -> MapstructUtils.convert(tenantRow, TenantBO.class)).thenReturn(tenant);
            mapped.when(() -> MapstructUtils.convert(roleRow, RoleBO.class)).thenReturn(role);
            assertSame(tenant, repository.selectTenantById(new TenantId(7L)));
            assertSame(role, repository.selectRoleById(3L));
            assertSame(role, repository.selectEnabledRoleByCode(new TenantId(7L), "editor"));
            assertSame(role, repository.selectTenantSuperRole(new TenantId(7L)));
        }
        assertEquals("ShiYu", repository.selectTenantNameById(new TenantId(7L)));
        verify(roles, times(2)).selectOneByQuery(any());
    }
}

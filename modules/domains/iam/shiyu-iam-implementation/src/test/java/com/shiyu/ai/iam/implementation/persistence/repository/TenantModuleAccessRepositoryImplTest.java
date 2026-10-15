package com.shiyu.ai.iam.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.iam.implementation.persistence.dataobject.TenantModuleAccessDO;
import com.shiyu.ai.iam.implementation.persistence.mapper.TenantModuleAccessMapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/** 验证租户隔离和模块默认配置的幂等性。 */
class TenantModuleAccessRepositoryImplTest {
    private TenantModuleAccessMapper mapper;
    private TenantModuleAccessRepositoryImpl repository;
    private final TenantId tenant = new TenantId(9L);

    @BeforeEach
    void setUp() {
        TenantScope.set(tenant);
        mapper = mock(TenantModuleAccessMapper.class);
        repository = new TenantModuleAccessRepositoryImpl();
        ReflectionTestUtils.setField(repository, "mapper", mapper);
    }

    @AfterEach
    void clearTenant() {
        TenantScope.clear();
    }

    @Test
    void refusesCrossTenantAndInvalidModuleQueries() {
        assertThrows(IllegalArgumentException.class, () -> repository.isEnabled(null, "education"));
        assertThrows(IllegalArgumentException.class, () -> repository.isEnabled(tenant, " "));
        assertThrows(IllegalArgumentException.class, () -> repository.isEnabled(new TenantId(10L), "education"));
        verify(mapper, never()).selectOneByQuery(any(QueryWrapper.class));
    }

    @Test
    void reportsOnlyEnabledModuleRows() {
        assertFalse(repository.isEnabled(tenant, "education"));
        TenantModuleAccessDO disabled = new TenantModuleAccessDO();
        disabled.setStatus(0);
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(disabled);
        assertFalse(repository.isEnabled(tenant, "education"));
        disabled.setStatus(1);
        assertTrue(repository.isEnabled(tenant, "education"));
    }

    @Test
    void initializesOnlyMissingNormalizedModules() {
        TenantModuleAccessDO existing = new TenantModuleAccessDO();
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null, existing);
        repository.initializeTenantDefaults(tenant, null);
        repository.initializeTenantDefaults(tenant, Arrays.asList("  education  ", null, "", "knowledge"));

        ArgumentCaptor<TenantModuleAccessDO> inserted = ArgumentCaptor.forClass(TenantModuleAccessDO.class);
        verify(mapper).insert(inserted.capture());
        assertEquals(tenant.value(), inserted.getValue().getTenantId());
        assertEquals("education", inserted.getValue().getModuleId());
        assertEquals(1, inserted.getValue().getStatus());
    }
}

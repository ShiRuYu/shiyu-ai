package com.shiyu.ai.record.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.record.implementation.persistence.mapper.ProfileMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileRepositoryTenantTest {
    @Test
    void readsAndDeletesAlwaysCarryTenantPredicate() throws Exception {
        ProfileMapper mapper = mock(ProfileMapper.class);
        when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(null);
        ProfileRepositoryImpl repository = new ProfileRepositoryImpl();
        Field field = ProfileRepositoryImpl.class.getDeclaredField("profileMapper");
        field.setAccessible(true);
        field.set(repository, mapper);

        TenantId tenant = new TenantId(17);
        repository.selectById(tenant, 42L);
        repository.deleteById(tenant, 42L);

        var query = org.mockito.ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectOneByQuery(query.capture());
        assertTrue(query.getValue().toSQL().toUpperCase().contains("TENANT_ID"));
        var deleteQuery = org.mockito.ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).deleteByQuery(deleteQuery.capture());
        assertTrue(deleteQuery.getValue().toSQL().toUpperCase().contains("TENANT_ID"));
    }
}

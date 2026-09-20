package com.shiyu.ai.agent.implementation.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.implementation.domain.model.AuditLogBO;
import com.shiyu.ai.agent.implementation.persistence.dataobject.AuditLogDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.AuditLogMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

/**
 * 验证 Audit Log Repository Impl 相关功能、边界条件、异常路径和协作行为。
 */
class AuditLogRepositoryImplTest {
    @BeforeEach
    void bindTenantScope() { TenantScope.set(new TenantId(7L)); }
    @AfterEach
    void clearTenantScope() { TenantScope.clear(); }
    @Test
    void rejectsUnattributedAuditRecordsAndPersistsTenantScopedRecords() throws Exception {
        AuditLogMapper mapper = mock(AuditLogMapper.class);
        AuditLogRepositoryImpl repository = new AuditLogRepositoryImpl();
        Field field = AuditLogRepositoryImpl.class.getDeclaredField("auditLogMapper");
        field.setAccessible(true);
        field.set(repository, mapper);

        AuditLogBO record = new AuditLogBO();
        assertThrows(IllegalArgumentException.class, () -> repository.insert(null, record));
        assertThrows(
                IllegalArgumentException.class, () -> repository.insert(new TenantId(0L), record));
        assertThrows(
                IllegalArgumentException.class, () -> repository.insert(new TenantId(7L), null));
        assertThrows(
                IllegalArgumentException.class, () -> repository.insert(new TenantId(7L), record));
        record.setUserId(0L);
        assertThrows(
                IllegalArgumentException.class, () -> repository.insert(new TenantId(7L), record));

        record.setUserId(11L);
        AuditLogDO data = new AuditLogDO();
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions
                    .when(() -> MapstructUtils.convert(record, AuditLogDO.class))
                    .thenReturn(data);
            repository.insert(new TenantId(7L), record);
        }
        assertEquals(7L, record.getTenantId());
        verify(mapper).insertSelective(data);
    }
}

package com.shiyu.ai.agent.persistence.repository;

import com.shiyu.ai.agent.domain.model.AuditLogBO;
import com.shiyu.ai.agent.persistence.dataobject.AuditLogDO;
import com.shiyu.ai.agent.persistence.mapper.AuditLogMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.kernel.context.TenantId;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogRepositoryImplTest {
    @Test
    void rejectsUnattributedAuditRecordsAndPersistsTenantScopedRecords() throws Exception {
        AuditLogMapper mapper = mock(AuditLogMapper.class);
        AuditLogRepositoryImpl repository = new AuditLogRepositoryImpl();
        Field field = AuditLogRepositoryImpl.class.getDeclaredField("auditLogMapper");
        field.setAccessible(true);
        field.set(repository, mapper);

        AuditLogBO record = new AuditLogBO();
        assertThrows(IllegalArgumentException.class, () -> repository.insert(null, record));
        assertThrows(IllegalArgumentException.class, () -> repository.insert(new TenantId(0L), record));
        assertThrows(IllegalArgumentException.class, () -> repository.insert(new TenantId(7L), null));
        assertThrows(IllegalArgumentException.class, () -> repository.insert(new TenantId(7L), record));
        record.setUserId(0L);
        assertThrows(IllegalArgumentException.class, () -> repository.insert(new TenantId(7L), record));

        record.setUserId(11L);
        AuditLogDO data = new AuditLogDO();
        try (MockedStatic<MapstructUtils> conversions = mockStatic(MapstructUtils.class)) {
            conversions.when(() -> MapstructUtils.convert(record, AuditLogDO.class)).thenReturn(data);
            repository.insert(new TenantId(7L), record);
        }
        assertEquals(7L, record.getTenantId());
        verify(mapper).insertSelective(data);
    }
}

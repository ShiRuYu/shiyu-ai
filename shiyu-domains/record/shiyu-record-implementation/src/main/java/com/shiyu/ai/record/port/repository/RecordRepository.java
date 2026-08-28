package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.record.domain.model.RecordBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface RecordRepository {
    Pair<Long, List<RecordBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, Long eventId);
    RecordBO selectById(TenantId tenantId, Long id);
    RecordBO insert(TenantId tenantId, RecordBO recordBO);
    boolean update(TenantId tenantId, RecordBO recordBO);
    boolean deleteById(TenantId tenantId, Long id);
}

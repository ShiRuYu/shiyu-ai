package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.record.domain.model.TimelineEventBO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface TimelineEventRepository {
    Pair<Long, List<TimelineEventBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, Long profileId);
    TimelineEventBO selectByIdWithDetails(TenantId tenantId, Long id);
    TimelineEventBO insert(TenantId tenantId, TimelineEventBO eventBO);
    boolean update(TenantId tenantId, TimelineEventBO eventBO);
    boolean deleteById(TenantId tenantId, Long id);
    List<TimelineEventBO> selectByProfileId(TenantId tenantId, Long profileId);
}

package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.record.domain.model.MediaBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface MediaRepository {
    Pair<Long, List<MediaBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, Long recordId);
    MediaBO selectById(TenantId tenantId, Long id);
    MediaBO insert(TenantId tenantId, MediaBO mediaBO);
    boolean update(TenantId tenantId, MediaBO mediaBO);
    boolean deleteById(TenantId tenantId, Long id);
}

package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.DictBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface DictRepository {
    Pair<Long, List<DictBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize);
    List<DictBO> selectAll(TenantId tenantId);
    DictBO selectById(TenantId tenantId, Long id);
    List<DictBO> selectByDictType(TenantId tenantId, String dictType);
    DictBO create(DictBO dictBO);
    DictBO update(DictBO dictBO);
    void deleteById(TenantId tenantId, Long id);
    void deleteByIds(TenantId tenantId, List<Long> ids);
}

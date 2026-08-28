package com.shiyu.ai.record.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.record.domain.model.TagBO;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface TagRepository {
    Pair<Long, List<TagBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name);
    TagBO selectById(TenantId tenantId, Long id);
    TagBO selectByName(TenantId tenantId, String name);
    List<TagBO> selectAll(TenantId tenantId);
    TagBO insert(TenantId tenantId, TagBO tagBO);
    boolean update(TenantId tenantId, TagBO tagBO);
    boolean deleteById(TenantId tenantId, Long id);
}

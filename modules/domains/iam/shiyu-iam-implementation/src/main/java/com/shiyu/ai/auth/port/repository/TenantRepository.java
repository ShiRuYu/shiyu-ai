package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public interface TenantRepository {
    Pair<Long, List<TenantBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name, String code, Integer status);
    List<TenantBO> selectAll();
    TenantBO selectById(Long id);
    Long selectRootTenantId(TenantId tenantId);
    TenantBO insert(TenantBO tenantBO, TenantId sourceTenantId);
    boolean update(TenantBO tenantBO);
    boolean deleteById(Long id);
    boolean existsByCode(String code, Long excludeId);
    void cascadeDelete(TenantId tenantId);
    List<Long> selectDescendantIds(TenantId rootId);
}

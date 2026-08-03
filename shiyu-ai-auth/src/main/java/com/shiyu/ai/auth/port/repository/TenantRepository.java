package com.shiyu.ai.auth.port.repository;

import com.shiyu.ai.auth.domain.model.TenantBO;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public interface TenantRepository {
    Pair<Long, List<TenantBO>> selectPage(Number pageNo, Number pageSize, String name, String code, Integer status);
    List<TenantBO> selectAll();
    TenantBO selectById(Long id);
    Long selectRootTenantId(Long tenantId);
    TenantBO insert(TenantBO tenantBO);
    boolean update(TenantBO tenantBO);
    boolean deleteById(Long id);
    boolean existsByCode(String code, Long excludeId);
    void cascadeDelete(Long tenantId);
    List<Long> selectDescendantIds(Long rootId);
}

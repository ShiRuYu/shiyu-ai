package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.IntentDefBO;
import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface IntentDefRepository {
    Pair<Long, List<IntentDefBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String agentId, String name, String code, String category);
    List<IntentDefBO> selectByAgentId(TenantId tenantId, String agentId);
    List<IntentDefBO> selectByCategory(TenantId tenantId, String agentId, String category);
    IntentDefBO selectById(TenantId tenantId, Long id);
    IntentDefBO create(TenantId tenantId, IntentDefBO bo);
    IntentDefBO update(TenantId tenantId, IntentDefBO bo);
    void deleteById(TenantId tenantId, Long id);
    void deleteByIds(TenantId tenantId, List<Long> ids);
    List<IdNameOptionVO> selectAllOptions(TenantId tenantId);
}

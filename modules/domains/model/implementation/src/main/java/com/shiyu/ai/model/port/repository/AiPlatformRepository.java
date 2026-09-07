package com.shiyu.ai.model.port.repository;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.domain.model.AiPlatformBO;
import com.shiyu.ai.kernel.context.TenantId;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface AiPlatformRepository {
    Pair<Long, List<AiPlatformBO>> selectPage(TenantId tenantId, Number pageNo, Number pageSize, String name, String code);
    List<AiPlatformBO> selectAllEnabled(TenantId tenantId);
    AiPlatformBO selectById(TenantId tenantId, Long id);
    AiPlatformBO selectByCode(TenantId tenantId, String code);
    AiPlatformBO selectDefault(TenantId tenantId);
    AiPlatformBO create(TenantId tenantId, AiPlatformBO bo);
    AiPlatformBO update(TenantId tenantId, AiPlatformBO bo);
    void deleteById(TenantId tenantId, Long id);
    List<IdNameOptionVO> selectOptions(TenantId tenantId);
    void clearDefaultExcept(TenantId tenantId, Long excludeId);
}

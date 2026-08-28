package com.shiyu.ai.model.port.repository;

import com.shiyu.ai.common.core.vo.IdNameOptionVO;
import com.shiyu.ai.model.domain.model.AiModelBO;
import com.shiyu.ai.kernel.context.TenantId;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface AiModelRepository {
    Pair<Long, List<AiModelBO>> selectPage(TenantId tenantId, Long platformId, Number pageNo, Number pageSize);
    List<AiModelBO> selectByPlatformId(TenantId tenantId, Long platformId);
    AiModelBO selectById(TenantId tenantId, Long id);
    AiModelBO selectDefaultByPlatformId(TenantId tenantId, Long platformId);
    AiModelBO create(TenantId tenantId, AiModelBO bo);
    AiModelBO update(TenantId tenantId, AiModelBO bo);
    void deleteById(TenantId tenantId, Long id);
    void deleteByIds(TenantId tenantId, List<Long> ids);
    List<IdNameOptionVO> selectOptions(TenantId tenantId, Long platformId);
    void clearDefaultExcept(TenantId tenantId, Long platformId, Long excludeId);
}

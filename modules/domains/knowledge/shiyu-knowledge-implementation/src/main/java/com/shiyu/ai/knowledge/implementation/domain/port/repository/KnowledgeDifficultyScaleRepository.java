package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleLevelBO;

import java.util.List;

/**
 * KnowledgeDifficultyScaleRepository 仓储接口，负责访问和持久化知识领域聚合数据。
 */
public interface KnowledgeDifficultyScaleRepository {
    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param scaleId 方法参数。
     *
     * @return 操作结果。
     */
    KnowledgeDifficultyScaleBO findScale(TenantId tenantId, Long scaleId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param scaleId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<KnowledgeDifficultyScaleLevelBO> findLevels(TenantId tenantId, Long scaleId);
}

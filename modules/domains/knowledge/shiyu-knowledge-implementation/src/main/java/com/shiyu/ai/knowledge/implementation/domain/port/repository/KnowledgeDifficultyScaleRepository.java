package com.shiyu.ai.knowledge.implementation.domain.port.repository;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.implementation.domain.model.KnowledgeDifficultyScaleLevelBO;

import java.util.List;

public interface KnowledgeDifficultyScaleRepository {
    KnowledgeDifficultyScaleBO findScale(TenantId tenantId, Long scaleId);

    List<KnowledgeDifficultyScaleLevelBO> findLevels(TenantId tenantId, Long scaleId);
}

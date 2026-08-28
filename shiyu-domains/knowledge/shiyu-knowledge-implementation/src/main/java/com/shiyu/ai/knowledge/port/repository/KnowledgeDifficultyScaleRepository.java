package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleLevelBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface KnowledgeDifficultyScaleRepository {
    KnowledgeDifficultyScaleBO findScale(TenantId tenantId, Long scaleId);
    List<KnowledgeDifficultyScaleLevelBO> findLevels(TenantId tenantId, Long scaleId);
}

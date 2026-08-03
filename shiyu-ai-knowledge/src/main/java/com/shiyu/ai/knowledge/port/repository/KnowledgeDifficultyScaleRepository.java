package com.shiyu.ai.knowledge.port.repository;

import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleLevelBO;
import java.util.List;

public interface KnowledgeDifficultyScaleRepository {
    KnowledgeDifficultyScaleBO findScale(Long scaleId);
    List<KnowledgeDifficultyScaleLevelBO> findLevels(Long scaleId);
}

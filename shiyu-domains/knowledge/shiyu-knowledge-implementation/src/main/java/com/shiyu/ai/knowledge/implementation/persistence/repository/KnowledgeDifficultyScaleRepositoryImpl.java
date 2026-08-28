package com.shiyu.ai.knowledge.implementation.persistence.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDifficultyScaleDO;
import com.shiyu.ai.knowledge.implementation.persistence.dataobject.KnowledgeDifficultyScaleLevelDO;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleLevelMapper;
import com.shiyu.ai.knowledge.implementation.persistence.mapper.KnowledgeDifficultyScaleMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleLevelBO;
import com.shiyu.ai.kernel.context.TenantId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class KnowledgeDifficultyScaleRepositoryImpl implements com.shiyu.ai.knowledge.port.repository.KnowledgeDifficultyScaleRepository {

    private final KnowledgeDifficultyScaleMapper scaleMapper;
    private final KnowledgeDifficultyScaleLevelMapper levelMapper;

    public KnowledgeDifficultyScaleBO findScale(TenantId tenantId, Long scaleId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(scaleMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDifficultyScaleDO::getId, scaleId)
                .eq(KnowledgeDifficultyScaleDO::getTenantId, tenantId.value())
                .eq(KnowledgeDifficultyScaleDO::getDelFlag, 0)
                .eq(KnowledgeDifficultyScaleDO::getStatus, 1)), KnowledgeDifficultyScaleBO.class);
    }

    public List<KnowledgeDifficultyScaleLevelBO> findLevels(TenantId tenantId, Long scaleId) {
        requireTenant(tenantId);
        return MapstructUtils.convert(levelMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeDifficultyScaleLevelDO::getScaleId, scaleId)
                .eq(KnowledgeDifficultyScaleLevelDO::getTenantId, tenantId.value())
                .eq(KnowledgeDifficultyScaleLevelDO::getDelFlag, 0)
                .eq(KnowledgeDifficultyScaleLevelDO::getStatus, 1)
                .orderBy(KnowledgeDifficultyScaleLevelDO::getLevel, true)), KnowledgeDifficultyScaleLevelBO.class);
    }

    private static void requireTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
    }
}


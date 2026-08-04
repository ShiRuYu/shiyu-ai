package com.shiyu.ai.dal.knowledge.repository;

import com.mybatisflex.core.query.QueryWrapper;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDifficultyScaleDO;
import com.shiyu.ai.dal.knowledge.dataobject.KnowledgeDifficultyScaleLevelDO;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDifficultyScaleLevelMapper;
import com.shiyu.ai.dal.knowledge.mapper.KnowledgeDifficultyScaleMapper;
import com.shiyu.ai.common.core.utils.MapstructUtils;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleBO;
import com.shiyu.ai.knowledge.domain.model.KnowledgeDifficultyScaleLevelBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class KnowledgeDifficultyScaleRepository implements com.shiyu.ai.knowledge.port.repository.KnowledgeDifficultyScaleRepository {

    private final KnowledgeDifficultyScaleMapper scaleMapper;
    private final KnowledgeDifficultyScaleLevelMapper levelMapper;

    public KnowledgeDifficultyScaleBO findScale(Long scaleId) {
        return MapstructUtils.convert(scaleMapper.selectOneByQuery(QueryWrapper.create()
                .eq(KnowledgeDifficultyScaleDO::getId, scaleId)
                .eq(KnowledgeDifficultyScaleDO::getDelFlag, 0)
                .eq(KnowledgeDifficultyScaleDO::getStatus, 1)), KnowledgeDifficultyScaleBO.class);
    }

    public List<KnowledgeDifficultyScaleLevelBO> findLevels(Long scaleId) {
        return MapstructUtils.convert(levelMapper.selectListByQuery(QueryWrapper.create()
                .eq(KnowledgeDifficultyScaleLevelDO::getScaleId, scaleId)
                .eq(KnowledgeDifficultyScaleLevelDO::getDelFlag, 0)
                .eq(KnowledgeDifficultyScaleLevelDO::getStatus, 1)
                .orderBy(KnowledgeDifficultyScaleLevelDO::getLevel, true)), KnowledgeDifficultyScaleLevelBO.class);
    }
}
